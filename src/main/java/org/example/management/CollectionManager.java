package org.example.management;

import org.example.data.Product;
import org.example.data.User;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class CollectionManager {
    private final LinkedList<Product> collection = new LinkedList<>();
    private ZonedDateTime initTime;
    private final DatabaseManager databaseManager;

    public CollectionManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public synchronized void loadFromDatabase() throws Exception {
        collection.clear();
        List<Product> products = databaseManager.loadProducts();
        collection.addAll(products);

        if (!collection.isEmpty()) {
            initTime = ZonedDateTime.now();
            collection.sort(Product::compareTo);
        }
    }

    public synchronized boolean addProduct(Product product, User user) throws Exception {
        Long id = databaseManager.addProduct(product, user.getId());
        if (id != null && id > 0) {
            product.setId(id);
            product.setCreatorId(user.getId());
            product.setCreationDate(ZonedDateTime.now());

            if (collection.isEmpty()) {
                this.initTime = product.getCreationDate();
            }

            boolean added = collection.add(product);
            if (added) {
                // Поддерживаем сортировку после добавления
                collection.sort(Product::compareTo);
            }
            return added;
        }
        return false;
    }

    public synchronized boolean removeById(Long id, User user) throws Exception {
        Product product = getById(id);
        if (product != null && product.getCreatorId() == user.getId()) {
            if (databaseManager.removeProduct(id)) {
                boolean removed = collection.removeIf(p -> p.getId().equals(id));
                if (removed) {
                    // Поддерживаем сортировку после удаления
                    collection.sort(Product::compareTo);
                }
                return removed;
            }
        }
        return false;
    }

    public synchronized void clear(User user) throws Exception {
        // Удаляем только продукты, созданные данным пользователем
        if (databaseManager.clearUserProducts(user.getId())) {
            collection.removeIf(p -> p.getCreatorId() == user.getId());
        }
    }

    public synchronized Product head() {
        return collection.peekFirst();
    }

    public synchronized Product removeHead(User user) throws Exception {
        Product product = collection.peekFirst();
        if (product != null && product.getCreatorId() == user.getId()) {
            if (databaseManager.removeProduct(product.getId())) {
                Product removed = collection.pollFirst();
                // Поддерживаем сортировку после удаления
                collection.sort(Product::compareTo);
                return removed;
            }
        }
        return null;
    }

    public LinkedList<Product> getCollection() {
        return new LinkedList<>(collection);
    }

    public double getAverageManufactureCost() {
        return collection.stream()
                .filter(p -> p.getManufactureCost() != null)
                .mapToDouble(Product::getManufactureCost)
                .average()
                .orElse(0.0);
    }

    public ZonedDateTime getInitTime() {
        return initTime;
    }

    public Product getById(Long id) {
        return collection.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public Product getMin() {
        return collection.stream()
                .min(Product::compareTo)
                .orElse(null);
    }

    public synchronized boolean updateProduct(Long id, Product newProduct, User user) throws Exception {
        Product oldProduct = getById(id);
        if (oldProduct != null && oldProduct.getCreatorId() == user.getId()) {
            if (databaseManager.updateProduct(id, newProduct, user.getId())) {
                newProduct.setId(id);
                newProduct.setCreatorId(user.getId());
                newProduct.setCreationDate(oldProduct.getCreationDate());

                collection.remove(oldProduct);
                boolean added = collection.add(newProduct);
                if (added) {
                    // Поддерживаем сортировку после обновления
                    collection.sort(Product::compareTo);
                }
                return added;
            }
        }
        return false;
    }

    // Новые методы для дополнительных команд
    public synchronized void shuffle() {
        // Перемешиваем элементы коллекции
        List<Product> tempList = new LinkedList<>(collection);
        java.util.Collections.shuffle(tempList);
        collection.clear();
        collection.addAll(tempList);
    }

    public synchronized void sort() {
        // Сортируем коллекцию в естественном порядке
        collection.sort(Product::compareTo);
    }

    public List<Product> filterByPrice(Long price) {
        return collection.stream()
                .filter(p -> p.getPrice() != null && p.getPrice().equals(price))
                .collect(Collectors.toList());
    }

    public List<Product> getAscending() {
        return collection.stream()
                .sorted(Product::compareTo)
                .collect(Collectors.toList());
    }

    public List<Long> getAscendingPrices() {
        return collection.stream()
                .filter(p -> p.getPrice() != null)
                .map(Product::getPrice)
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return collection.stream()
                .map(Product::toString)
                .collect(Collectors.joining("\n"));
    }
}