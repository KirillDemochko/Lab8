package org.example.client.gui;

import org.example.client.gui.components.ProductTable;
import org.example.client.gui.components.VisualizationPanel;
import org.example.client.gui.dialogs.FilterDialog;
import org.example.client.gui.dialogs.ProductEditDialog;
import org.example.client.gui.dialogs.SortDialog;
import org.example.client.gui.models.ProductTableModel;
import org.example.client.gui.resources.Localization;
import org.example.client.network.GuiClient;
import org.example.client.state.SessionState;
import org.example.data.Product;
import org.example.data.User;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Comparator;
import java.util.Locale;

public class MainWindow extends JFrame implements ChangeListener, Localization.LocaleChangeListener {
    private final Localization localization;
    private final GuiClient client;
    private final SessionState sessionState;
    private ProductTableModel tableModel;
    private ProductTable productTable;
    private VisualizationPanel visualizationPanel;
    private JComboBox<String> languageComboBox;
    private JLabel userLabel;
    private JLabel statusLabel;
    private JLabel languageLabel;
    private JMenuBar menuBar;
    private JToolBar toolBar;
    private JButton addButton;
    private JButton updateButton;
    private JButton removeButton;
    private JButton filterButton;
    private JButton clearButton;
    private JButton executeButton;


    public MainWindow() {
        this.localization = Localization.getInstance();
        this.client = GuiClient.getInstance();
        this.sessionState = SessionState.getInstance();

        initComponents();
        setupWindow();
        loadProducts();

        sessionState.addChangeListener(this);
        localization.addLocaleChangeListener(this);
    }

    private void initComponents() {
        tableModel = new ProductTableModel(sessionState.getProducts());
        productTable = new ProductTable(tableModel);
        visualizationPanel = new VisualizationPanel();
        languageComboBox = new JComboBox<>();
        updateLanguageComboBox();
        userLabel = new JLabel();
        statusLabel = new JLabel(localization.getString("status.connected"));
        statusLabel.setForeground(Color.GREEN);
        languageLabel = new JLabel(localization.getString("menu.language") + ":");

        addButton = new JButton();
        updateButton = new JButton();
        removeButton = new JButton();
        filterButton = new JButton();
        clearButton = new JButton();
        executeButton = new JButton();

        updateLocalizedText();
        updateUserInfo();
    }

    private void setupWindow() {
        setTitle(localization.getString("application.title"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        setupMenuBar();
        setupToolBar();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(new JScrollPane(productTable));
        splitPane.setRightComponent(visualizationPanel);
        splitPane.setDividerLocation(600);

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        statusPanel.add(userLabel, BorderLayout.WEST);
        statusPanel.add(statusLabel, BorderLayout.EAST);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(toolBar, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        setJMenuBar(menuBar);

        client.startPeriodicUpdates(5, this::loadProducts);
    }

    private void setupMenuBar() {
        menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu(localization.getString("menu.file"));
        JMenuItem exitMenuItem = new JMenuItem(localization.getString("menu.exit"));
        exitMenuItem.setActionCommand("exit");
        exitMenuItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitMenuItem);

        JMenu viewMenu = new JMenu(localization.getString("menu.view"));
        JMenuItem languageMenuItem = new JMenuItem(localization.getString("menu.language"));
        languageMenuItem.setActionCommand("language");
        viewMenu.add(languageMenuItem);

        JMenu commandsMenu = new JMenu(localization.getString("menu.commands"));
        JMenuItem addMenuItem = new JMenuItem(localization.getString("button.add"));
        addMenuItem.setActionCommand("add");
        JMenuItem updateMenuItem = new JMenuItem(localization.getString("button.update"));
        updateMenuItem.setActionCommand("update");
        JMenuItem removeMenuItem = new JMenuItem(localization.getString("button.remove"));
        removeMenuItem.setActionCommand("remove");
        JMenuItem filterMenuItem = new JMenuItem(localization.getString("button.filter"));
        filterMenuItem.setActionCommand("filter");
        JMenuItem clearMenuItem = new JMenuItem(localization.getString("button.clear"));
        clearMenuItem.setActionCommand("clear");
        JMenuItem executeMenuItem = new JMenuItem(localization.getString("button.execute"));
        executeMenuItem.setActionCommand("execute");

        addMenuItem.addActionListener(this::addProduct);
        updateMenuItem.addActionListener(this::updateProduct);
        removeMenuItem.addActionListener(this::removeProduct);
        filterMenuItem.addActionListener(this::showFilterDialog);
        clearMenuItem.addActionListener(this::clearProducts);
        executeMenuItem.addActionListener(this::executeScript);

        commandsMenu.add(addMenuItem);
        commandsMenu.add(updateMenuItem);
        commandsMenu.add(removeMenuItem);
        commandsMenu.add(filterMenuItem);
        commandsMenu.add(clearMenuItem);
        commandsMenu.add(executeMenuItem);

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(commandsMenu);
    }

    private void setupToolBar() {
        toolBar = new JToolBar();
        toolBar.setFloatable(false);

        addButton.addActionListener(this::addProduct);
        updateButton.addActionListener(this::updateProduct);
        removeButton.addActionListener(this::removeProduct);
        filterButton.addActionListener(this::showFilterDialog);
        clearButton.addActionListener(this::clearProducts);
        executeButton.addActionListener(this::executeScript);
        //sortButton.addActionListener(this::sortProducts);

        toolBar.add(addButton);
        toolBar.add(updateButton);
        toolBar.add(removeButton);
        toolBar.add(filterButton);
        toolBar.add(clearButton);
        toolBar.add(executeButton);
        toolBar.addSeparator();
        //toolBar.add(sortButton);

        toolBar.add(Box.createHorizontalGlue());

        JPanel languagePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        languageLabel = new JLabel(localization.getString("menu.language") + ":");
        languagePanel.add(languageLabel);
        languagePanel.add(languageComboBox);
        toolBar.add(languagePanel);

        // Добавляем обработчик выбора языка
        languageComboBox.addActionListener(e -> {
            if (e.getActionCommand().equals("comboBoxChanged")) {
                String selected = (String) languageComboBox.getSelectedItem();
                if (selected == null) return;

                Locale newLocale = null;
                if (selected.equals(localization.getString("language.russian"))) {
                    newLocale = Localization.RUSSIAN;
                } else if (selected.equals(localization.getString("language.portuguese"))) {
                    newLocale = Localization.PORTUGUESE;
                } else if (selected.equals(localization.getString("language.polish"))) {
                    newLocale = Localization.POLISH;
                } else if (selected.equals(localization.getString("language.englishNZ"))) {
                    newLocale = Localization.ENGLISH_NZ;
                }

                if (newLocale != null && !newLocale.equals(localization.getCurrentLocale())) {
                    localization.setLocale(newLocale);
                }
            }
        });
    }

    private void updateLanguageComboBox() {
        // Временно удаляем слушатели
        ActionListener[] listeners = languageComboBox.getActionListeners();
        for (ActionListener listener : listeners) {
            languageComboBox.removeActionListener(listener);
        }

        languageComboBox.removeAllItems();
        languageComboBox.addItem(localization.getString("language.russian"));
        languageComboBox.addItem(localization.getString("language.portuguese"));
        languageComboBox.addItem(localization.getString("language.polish"));
        languageComboBox.addItem(localization.getString("language.englishNZ"));

        Locale currentLocale = localization.getCurrentLocale();
        if (currentLocale.equals(Localization.RUSSIAN)) {
            languageComboBox.setSelectedIndex(0);
        } else if (currentLocale.equals(Localization.PORTUGUESE)) {
            languageComboBox.setSelectedIndex(1);
        } else if (currentLocale.equals(Localization.POLISH)) {
            languageComboBox.setSelectedIndex(2);
        } else if (currentLocale.equals(Localization.ENGLISH_NZ)) {
            languageComboBox.setSelectedIndex(3);
        }

        // Восстанавливаем слушатели
        for (ActionListener listener : listeners) {
            languageComboBox.addActionListener(listener);
        }
    }

    private void updateUserInfo() {
        User user = sessionState.getCurrentUser();
        if (user != null) {
            String welcomeText = localization.getString("welcome");
            userLabel.setText(welcomeText.replace("{0}", user.getUsername()));
        }
    }

    private void updateLocalizedText() {
        System.out.println("MainWindow: Updating localized text for locale: " + localization.getCurrentLocale());
        setTitle(localization.getString("application.title"));
        addButton.setText(localization.getString("button.add"));
        updateButton.setText(localization.getString("button.update"));
        removeButton.setText(localization.getString("button.remove"));
        filterButton.setText(localization.getString("button.filter"));
        clearButton.setText(localization.getString("button.clear"));
        executeButton.setText(localization.getString("button.execute"));
        updateLanguageComboBox();
        languageLabel.setText(localization.getString("menu.language") + ":");
        //sortButton.setText(localization.getString("button.sort"));

        if (statusLabel.getText().startsWith(localization.getString("status.connected", true))) {
            statusLabel.setText(localization.getString("status.connected"));
        }

        if (menuBar != null) {
            for (int i = 0; i < menuBar.getMenuCount(); i++) {
                JMenu menu = menuBar.getMenu(i);
                switch (i) {
                    case 0: menu.setText(localization.getString("menu.file")); break;
                    case 1: menu.setText(localization.getString("menu.view")); break;
                    case 2: menu.setText(localization.getString("menu.commands")); break;
                }
                for (Component comp : menu.getMenuComponents()) {
                    if (comp instanceof JMenuItem menuItem) {
                        String actionCommand = menuItem.getActionCommand();
                        if (actionCommand != null) {
                            switch (actionCommand) {
                                case "exit":
                                    menuItem.setText(localization.getString("menu.exit"));
                                    break;
                                case "language":
                                    menuItem.setText(localization.getString("menu.language"));
                                    break;
                                case "add":
                                    menuItem.setText(localization.getString("button.add"));
                                    break;
                                case "update":
                                    menuItem.setText(localization.getString("button.update"));
                                    break;
                                case "remove":
                                    menuItem.setText(localization.getString("button.remove"));
                                    break;
                                case "filter":
                                    menuItem.setText(localization.getString("button.filter"));
                                    break;
                                case "clear":
                                    menuItem.setText(localization.getString("button.clear"));
                                    break;
                                case "execute":
                                    menuItem.setText(localization.getString("button.execute"));
                                    break;
                            }
                        }
                    }
                }
            }
        }
        tableModel.fireTableDataChanged();
        tableModel.updateTableHeaders();
    }

    private void loadProducts() {
        client.requestProducts(
                products -> {
                    sessionState.setProducts(products);
                    statusLabel.setText(localization.getString("status.lastUpdate") + new java.util.Date());
                    statusLabel.setForeground(Color.GREEN);
                },
                error -> {
                    statusLabel.setText(localization.getString("status.error") + error);
                    statusLabel.setForeground(Color.RED);
                }
        );
    }

    private void showLanguageDialog() {
        JDialog dialog = new JDialog(this, localization.getString("menu.language"), true);
        dialog.setLayout(new FlowLayout());

        JButton ruButton = new JButton(localization.getString("language.russian"));
        ruButton.addActionListener(e -> {
            localization.setLocale(Localization.RUSSIAN);
            dialog.dispose();
        });

        JButton ptButton = new JButton(localization.getString("language.portuguese"));
        ptButton.addActionListener(e -> {
            localization.setLocale(Localization.PORTUGUESE);
            dialog.dispose();
        });

        JButton plButton = new JButton(localization.getString("language.polish"));
        plButton.addActionListener(e -> {
            localization.setLocale(Localization.POLISH);
            dialog.dispose();
        });

        JButton enButton = new JButton(localization.getString("language.englishNZ"));
        enButton.addActionListener(e -> {
            localization.setLocale(Localization.ENGLISH_NZ);
            dialog.dispose();
        });

        dialog.add(ruButton);
        dialog.add(ptButton);
        dialog.add(plButton);
        dialog.add(enButton);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void addProduct(ActionEvent e) {
        ProductEditDialog dialog = new ProductEditDialog(this, null);
        dialog.setVisible(true);
    }

    private void updateProduct(ActionEvent e) {
        Product selectedProduct = productTable.getSelectedProduct();
        if (selectedProduct != null) {
            if (selectedProduct.getCreatorId() == sessionState.getCurrentUser().getId()) {
                ProductEditDialog dialog = new ProductEditDialog(this, selectedProduct);
                dialog.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this,
                        localization.getString("error.editOtherUser"),
                        localization.getString("error.title"),
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    localization.getString("error.selectProduct"),
                    localization.getString("error.title"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeProduct(ActionEvent e) {
        Product selectedProduct = productTable.getSelectedProduct();
        if (selectedProduct != null) {
            if (selectedProduct.getCreatorId() == sessionState.getCurrentUser().getId()) {
                Object[] options = {
                        localization.getString("option.yes"),
                        localization.getString("option.no")
                };

                int result = JOptionPane.showOptionDialog(this,
                        localization.getString("confirm.delete"),
                        localization.getString("confirm.title"),
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]);

                if (result == JOptionPane.YES_OPTION) {
                    String[] args = {String.valueOf(selectedProduct.getId())};
                    client.sendCommandRequest("remove_by_id", args, sessionState.getCurrentUser(),
                            response -> {
                                if (response.isSuccess()) {
                                    JOptionPane.showMessageDialog(this,
                                            response.getMessage(),
                                            localization.getString("success.title"),
                                            JOptionPane.INFORMATION_MESSAGE);
                                    loadProducts();
                                } else {
                                    JOptionPane.showMessageDialog(this,
                                            response.getMessage(),
                                            localization.getString("error.title"),
                                            JOptionPane.ERROR_MESSAGE);
                                }
                            });
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        localization.getString("error.deleteOtherUser"),
                        localization.getString("error.title"),
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    localization.getString("error.selectProduct"),
                    localization.getString("error.title"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editProduct(Product product) {
        if (product.getCreatorId() == sessionState.getCurrentUser().getId()) {
            ProductEditDialog dialog = new ProductEditDialog(this, product);
            dialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this,
                    localization.getString("error.editOtherUser"),
                    localization.getString("error.title"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }


    private void showFilterDialog(ActionEvent e) {
        FilterDialog dialog = new FilterDialog(this, tableModel);
        dialog.setVisible(true);
    }

    private void clearProducts(ActionEvent e) {
        Object[] options = {
                localization.getString("option.yes"),
                localization.getString("option.no")
        };

        int result = JOptionPane.showOptionDialog(this,
                localization.getString("confirm.clear"),
                localization.getString("confirm.title"),
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (result == JOptionPane.YES_OPTION) {
            client.sendCommandRequest("clear", new String[0], sessionState.getCurrentUser(),
                    response -> {
                        if (response.isSuccess()) {
                            JOptionPane.showMessageDialog(this,
                                    response.getMessage(),
                                    localization.getString("success.title"),
                                    JOptionPane.INFORMATION_MESSAGE);
                            loadProducts();
                        } else {
                            JOptionPane.showMessageDialog(this,
                                    response.getMessage(),
                                    localization.getString("error.title"),
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    });
        }
    }

    private void executeScript(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            String[] args = {fileChooser.getSelectedFile().getAbsolutePath()};
            client.sendCommandRequest("execute_script", args, sessionState.getCurrentUser(),
                    response -> {
                        if (response.isSuccess()) {
                            JOptionPane.showMessageDialog(this,
                                    response.getMessage(),
                                    localization.getString("script.title"),
                                    JOptionPane.INFORMATION_MESSAGE);
                            loadProducts();
                        } else {
                            JOptionPane.showMessageDialog(this,
                                    response.getMessage(),
                                    localization.getString("script.error"),
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    });
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        tableModel.setProducts(sessionState.getProducts());
        visualizationPanel.repaint();
        updateUserInfo();
    }

    private Locale lastLocale = null;

    @Override
    public void onLocaleChanged(Locale newLocale) {
        System.out.println("MainWindow: Locale change detected: " + newLocale);
        if (newLocale.equals(lastLocale)) {
            System.out.println("MainWindow: Skipping duplicate locale change for " + newLocale);
            return;
        }
        lastLocale = newLocale;

        SwingUtilities.invokeLater(() -> {
            updateLanguageComboBox();
            updateLocalizedText();
        });
    }

    public static void showMainWindow() {
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}