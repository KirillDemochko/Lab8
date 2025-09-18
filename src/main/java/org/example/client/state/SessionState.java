package org.example.client.state;

import org.example.data.User;
import org.example.data.Product;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.HashMap;
import java.util.Map;

public class SessionState {
    private static SessionState instance;
    private User currentUser;
    private List<Product> products;
    private Map<Integer, String> userColors;
    private List<ChangeListener> changeListeners;

    private SessionState() {
        products = new CopyOnWriteArrayList<>();
        userColors = new HashMap<>();
        changeListeners = new CopyOnWriteArrayList<>();
    }

    public static SessionState getInstance() {
        if (instance == null) {
            instance = new SessionState();
        }
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        notifyChangeListeners();
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products.clear();
        this.products.addAll(products);
        notifyChangeListeners();
    }

    public void addProduct(Product product) {
        this.products.add(product);
        notifyChangeListeners();
    }

    public void updateProduct(Product product) {
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId().equals(product.getId())) {
                products.set(i, product);
                break;
            }
        }
        notifyChangeListeners();
    }

    public void removeProduct(Long productId) {
        products.removeIf(p -> p.getId().equals(productId));
        notifyChangeListeners();
    }

    public String getUserColor(int userId) {
        if (!userColors.containsKey(userId)) {
            // Генерируем цвет на основе ID пользователя
            float hue = (userId * 0.618f) % 1; // Золотое сечение для распределения
            String color = String.format("#%06x", java.awt.Color.HSBtoRGB(hue, 0.8f, 0.8f) & 0xFFFFFF);
            userColors.put(userId, color);
        }
        return userColors.get(userId);
    }

    public void addChangeListener(ChangeListener listener) {
        changeListeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeListeners.remove(listener);
    }

    private void notifyChangeListeners() {
        ChangeEvent event = new ChangeEvent(this);
        for (ChangeListener listener : changeListeners) {
            listener.stateChanged(event);
        }
    }
}