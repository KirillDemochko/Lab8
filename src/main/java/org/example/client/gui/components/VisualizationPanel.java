package org.example.client.gui.components;

import org.example.client.animation.ProductAnimator;
import org.example.client.state.SessionState;
import org.example.data.Product;
import org.example.data.Coordinates;
import org.example.client.gui.resources.Localization;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VisualizationPanel extends JPanel {
    private final SessionState sessionState;
    private final ProductAnimator productAnimator;
    private final List<Spark> sparks;
    private Product selectedProduct;
    private Product hoveredProduct;
    private static final long REMOVE_EFFECT_DURATION = 1000;
    private static final double MIN_SCALE = 0.1;
    private static final double MAX_SCALE = 5.0;
    private double scale = 1.0;
    private Point translation = new Point(0, 0);
    private Point dragStart = null;
    private static final int PADDING = 50;
    private static final int AXIS_ARROW_SIZE = 8;
    private static final int SPARK_COUNT = 30;
    private static final int GRID_STEP = 50;
    private static final Random random = new Random();
    private long lastAnimationTime;
    private List<Product> previousProducts;
    private final Localization localization = Localization.getInstance();

    public VisualizationPanel() {
        this.sessionState = SessionState.getInstance();
        this.productAnimator = new ProductAnimator();
        this.sparks = new ArrayList<>();
        this.previousProducts = new ArrayList<>();
        this.lastAnimationTime = System.currentTimeMillis();

        setBackground(new Color(245, 245, 245));
        setPreferredSize(new Dimension(800, 600));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                dragStart = e.getPoint();
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragStart != null) {
                    int dx = e.getX() - dragStart.x;
                    int dy = e.getY() - dragStart.y;
                    translation.translate(dx, dy);
                    dragStart = e.getPoint();
                    repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                handleMouseMove(e);
            }
        });

        addMouseWheelListener(e -> {
            double scaleFactor = e.getWheelRotation() < 0 ? 1.1 : 0.9;
            double newScale = scale * scaleFactor;
            if (newScale < MIN_SCALE) newScale = MIN_SCALE;
            if (newScale > MAX_SCALE) newScale = MAX_SCALE;
            scale = newScale;
            repaint();
        });

        Timer animationTimer = new Timer(16, e -> updateAnimations());
        animationTimer.start();
    }

    private static class Spark {
        float x, y;
        float dx, dy;
        float size;
        Color color;
        float life;
        float maxLife;

        Spark(float x, float y, Color color) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.size = random.nextFloat() * 3 + 1;
            this.life = this.maxLife = random.nextFloat() * 0.5f + 0.5f;

            double angle = random.nextDouble() * Math.PI * 2;
            float speed = random.nextFloat() * 3 + 1;
            this.dx = (float) (Math.cos(angle) * speed);
            this.dy = (float) (Math.sin(angle) * speed);
        }

        boolean update(float deltaTime) {
            x += dx;
            y += dy;
            dy += 0.1f;
            life -= deltaTime;
            return life > 0;
        }

        void draw(Graphics2D g2d) {
            float alpha = life / maxLife;
            Color sparkColor = new Color(
                    color.getRed() / 255f,
                    color.getGreen() / 255f,
                    color.getBlue() / 255f,
                    alpha
            );

            g2d.setColor(sparkColor);
            g2d.fill(new Ellipse2D.Float(x - size/2, y - size/2, size, size));
        }
    }

    private void createSparks(float x, float y, Color color, int count) {
        for (int i = 0; i < count; i++) {
            sparks.add(new Spark(x, y, color));
        }
    }

    private void createSelectionEffect(Product product) {
        Point position = convertToScreen(product.getCoordinates());
        createSparks(position.x, position.y, Color.YELLOW, SPARK_COUNT);
    }

    private void createHoverEffect(Product product) {
        Point position = convertToScreen(product.getCoordinates());
        createSparks(position.x, position.y, Color.CYAN, SPARK_COUNT/3);
    }

    public void createAddEffect(Product product) {
        Point position = convertToScreen(product.getCoordinates());
        createSparks(position.x, position.y, Color.GREEN, SPARK_COUNT);
        productAnimator.addProduct(product);
    }

    public void createRemoveEffect(Product product) {
        Point position = convertToScreen(product.getCoordinates());
        createSparks(position.x, position.y, Color.RED, SPARK_COUNT);
        productAnimator.removeProduct(product.getId());
    }

    private void updateAnimations() {
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastAnimationTime) / 1000f;
        lastAnimationTime = currentTime;

        boolean needsRepaint = false;
        productAnimator.update();
        checkProductChanges();

        for (int i = sparks.size() - 1; i >= 0; i--) {
            Spark spark = sparks.get(i);
            if (!spark.update(deltaTime)) {
                sparks.remove(i);
            }
            needsRepaint = true;
        }

        if (needsRepaint) {
            repaint();
        }
    }

    private void checkProductChanges() {
        List<Product> currentProducts = sessionState.getProducts();

        for (Product product : currentProducts) {
            if (!previousProducts.contains(product)) {
                createAddEffect(product);
            }
        }

        for (Product product : previousProducts) {
            if (!currentProducts.contains(product)) {
                createRemoveEffect(product);
            }
        }

        previousProducts = new ArrayList<>(currentProducts);
    }

    private Point convertToScreen(Coordinates coords) {
        int centerX = getWidth() / 2 + translation.x;
        int centerY = getHeight() / 2 + translation.y;

        int screenX = (int) (centerX + coords.getX() * scale);
        int screenY = (int) (centerY - coords.getY() * scale);

        return new Point(screenX, screenY);
    }

    private void handleMouseClick(MouseEvent e) {
        Point clickPoint = e.getPoint();
        selectedProduct = null;

        for (Product product : sessionState.getProducts()) {
            Point position = convertToScreen(product.getCoordinates());
            int size = getProductSize(product);

            if (position.distance(clickPoint) <= size / 2) {
                selectedProduct = product;
                createSelectionEffect(product);
                break;
            }
        }

        if (selectedProduct != null) {
            showProductInfo(selectedProduct);
        }
        repaint();
    }

    private void handleMouseMove(MouseEvent e) {
        Point mousePoint = e.getPoint();
        Product oldHovered = hoveredProduct;
        hoveredProduct = null;

        for (Product product : sessionState.getProducts()) {
            Point position = convertToScreen(product.getCoordinates());
            int size = getProductSize(product);

            if (position.distance(mousePoint) <= size / 2) {
                hoveredProduct = product;
                break;
            }
        }

        if (hoveredProduct != null && hoveredProduct != oldHovered) {
            createHoverEffect(hoveredProduct);
        }
    }

    private void showProductInfo(Product product) {
        StringBuilder info = new StringBuilder();
        info.append(localization.getString("table.header.id"))
                .append(": ").append(product.getId()).append("\n");
        info.append(localization.getString("table.header.name"))
                .append(": ").append(product.getName()).append("\n");
        info.append(localization.getString("table.header.coordinates"))
                .append(": ").append(product.getCoordinates()).append("\n");
        info.append(localization.getString("table.header.price"))
                .append(": ").append(product.getPrice()).append("\n");
        info.append(localization.getString("table.header.creator"))
                .append(": ").append(product.getCreatorId());

        JOptionPane.showMessageDialog(this, info.toString(),
                "Product Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private int getProductSize(Product product) {
        return product.getPrice() != null ?
                Math.max(20, Math.min(100, product.getPrice().intValue() / 10)) : 30;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        drawGrid(g2d);
        drawAxes(g2d);

        for (Spark spark : sparks) {
            spark.draw(g2d);
        }

        drawProducts(g2d);

        if (selectedProduct != null) {
            drawSelection(g2d, selectedProduct);
        }
    }

    private void drawAxes(Graphics2D g2d) {
        int centerX = getWidth() / 2 + translation.x;
        int centerY = getHeight() / 2 + translation.y;

        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2.0f));

        g2d.drawLine(PADDING, centerY, getWidth() - PADDING, centerY);
        g2d.drawLine(centerX, PADDING, centerX, getHeight() - PADDING);

        drawArrow(g2d, getWidth() - PADDING, centerY, AXIS_ARROW_SIZE, 0);
        drawArrow(g2d, centerX, PADDING, AXIS_ARROW_SIZE, Math.PI / 2);

        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("X", getWidth() - PADDING + 15, centerY - 5);
        g2d.drawString("Y", centerX + 5, PADDING - 15);

        drawAxisLabels(g2d, centerX, centerY);
    }

    private void drawAxisLabels(Graphics2D g2d, int centerX, int centerY) {
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        g2d.setColor(Color.DARK_GRAY);

        for (int n = -GRID_STEP; n <= GRID_STEP; n += GRID_STEP) {
            if (n == 0) continue;

            int x = centerX + (int) (n * scale);
            int y = centerY - (int) (n * scale);

            if (x > PADDING && x < getWidth() - PADDING) {
                String label = String.valueOf(n);
                int labelWidth = g2d.getFontMetrics().stringWidth(label);
                g2d.drawString(label, x - labelWidth/2, centerY + 15);
            }

            if (y > PADDING && y < getHeight() - PADDING) {
                String label = String.valueOf(n);
                int labelHeight = g2d.getFontMetrics().getHeight();
                g2d.drawString(label, centerX + 5, y + labelHeight/3);
            }
        }
    }

    private void drawArrow(Graphics2D g2d, int x, int y, int size, double angle) {
        Path2D arrow = new Path2D.Float();
        arrow.moveTo(x, y);
        arrow.lineTo(x - size, y - size);
        arrow.lineTo(x - size, y + size);
        arrow.closePath();

        AffineTransform oldTransform = g2d.getTransform();
        AffineTransform transform = new AffineTransform();
        transform.rotate(angle, x, y);
        g2d.setTransform(transform);

        g2d.fill(arrow);
        g2d.setTransform(oldTransform);
    }

    private void drawGrid(Graphics2D g2d) {
        int centerX = getWidth() / 2 + translation.x;
        int centerY = getHeight() / 2 + translation.y;

        g2d.setColor(new Color(220, 220, 220));
        float[] dash = {2, 4};
        g2d.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10, dash, 0));

        for (int n = -GRID_STEP; n <= GRID_STEP; n += GRID_STEP) {
            if (n == 0) continue;

            int x = centerX + (int) (n * scale);
            int y = centerY - (int) (n * scale);

            if (x > PADDING && x < getWidth() - PADDING) {
                g2d.drawLine(x, PADDING, x, getHeight() - PADDING);
            }

            if (y > PADDING && y < getHeight() - PADDING) {
                g2d.drawLine(PADDING, y, getWidth() - PADDING, y);
            }
        }
    }

    private void drawProducts(Graphics2D g2d) {
        for (Product product : sessionState.getProducts()) {
            Point position = convertToScreen(product.getCoordinates());
            float animationState = productAnimator.getAnimationState(product.getId());
            Color color = getColorForUser(product.getCreatorId());

            drawProduct(g2d, product, position, color, animationState);
        }
    }

    private Color getColorForUser(int userId) {
        float hue = (userId * 0.618f) % 1;
        return Color.getHSBColor(hue, 0.8f, 0.8f);
    }

    private void drawProduct(Graphics2D g2d, Product product, Point position, Color color, float animationState) {
        // Проверка animationState
        if (animationState < 0 || Float.isNaN(animationState)) {
            System.err.println("Invalid animationState for product ID: " + product.getId() + ", value: " + animationState);
            animationState = 1.0f;
        }

        int size = (int) (getProductSize(product) * animationState);
        if (size <= 0) {
            // Пропускаем отрисовку, если размер недопустим
            return;
        }

        int alpha = (int) (255 * animationState);
        Color transparentColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);

        if (animationState < 1.0f) {
            float glowSize = size * (1 + (1 - animationState) * 0.5f);
            float glowAlpha = (1 - animationState) * 0.5f;

            if (glowSize <= 0) {
                System.err.println("Invalid glowSize for product ID: " + product.getId() + ", glowSize: " + glowSize);
            } else {
                RadialGradientPaint glowPaint = new RadialGradientPaint(
                        position.x, position.y, glowSize,
                        new float[] {0.0f, 1.0f},
                        new Color[] {
                                new Color(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, glowAlpha),
                                new Color(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, 0f)
                        }
                );

                g2d.setPaint(glowPaint);
                g2d.fill(new Ellipse2D.Double(
                        position.x - glowSize/2, position.y - glowSize/2, glowSize, glowSize
                ));
            }
        }

        g2d.setColor(transparentColor);
        Ellipse2D circle = new Ellipse2D.Double(
                position.x - size/2, position.y - size/2, size, size
        );
        g2d.fill(circle);

        g2d.setColor(Color.BLACK);
        g2d.draw(circle);

        if (size > 20) {
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.PLAIN, Math.max(8, size/4)));

            String name = product.getName();
            if (name == null) {
                System.err.println("Null name for product ID: " + product.getId());
                name = "Unknown";
            }
            if (name.length() > 10) {
                name = name.substring(0, 7) + "...";
            }

            FontMetrics metrics = g2d.getFontMetrics();
            int textWidth = metrics.stringWidth(name);
            int textHeight = metrics.getHeight();

            g2d.drawString(name, position.x - textWidth/2, position.y + textHeight/4);
        }
    }


    private void drawSelection(Graphics2D g2d, Product product) {
        Point position = convertToScreen(product.getCoordinates());
        int size = getProductSize(product) + 10;

        g2d.setColor(Color.YELLOW);
        g2d.setStroke(new BasicStroke(2.0f));
        Ellipse2D selection = new Ellipse2D.Double(
                position.x - size/2, position.y - size/2, size, size
        );
        g2d.draw(selection);

        long time = System.currentTimeMillis();
        float pulse = (float) (0.5f + 0.5f * Math.sin(time * 0.01));
        int pulseSize = (int) (size * (1 + pulse * 0.1f));

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.draw(new Ellipse2D.Double(
                position.x - pulseSize/2, position.y - pulseSize/2, pulseSize, pulseSize
        ));
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

    public void resetView() {
        scale = 1.0;
        translation = new Point(0, 0);
        repaint();
    }
}