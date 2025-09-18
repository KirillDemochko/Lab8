package org.example.commands;

import org.example.data.Product;
import org.example.data.User;
import org.example.management.CollectionManager;
import org.example.management.DatabaseManager;

public class RemoveHeadCommand implements Command {
    private final CollectionManager collectionManager;
    private final DatabaseManager databaseManager;

    public RemoveHeadCommand(CollectionManager collectionManager, DatabaseManager databaseManager) {
        this.collectionManager = collectionManager;
        this.databaseManager = databaseManager;
    }

    @Override
    public String execute(String[] args, User user) {
        if (user == null) {
            return "Ошибка: требуется авторизация";
        }

        try {
            Product product = collectionManager.removeHead(user);

            if (product == null) {
                return "Коллекция пуста или у вас нет прав для удаления первого элемента";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Удален следующий элемент (с наименьшим manufactureCost):\n");
            sb.append("----------------------------------------\n");
            sb.append("ID: ").append(product.getId()).append("\n");
            sb.append("Название: '").append(product.getName()).append("'\n");
            sb.append("Координаты: (").append(product.getCoordinates().getX()).append(", ")
                    .append(product.getCoordinates().getY()).append(")\n");
            sb.append("Дата создания: ").append(product.getCreationDate()).append("\n");
            sb.append("Цена: ").append(product.getPrice()).append("\n");
            sb.append("Парт-номер: '").append(product.getPartNumber()).append("'\n");
            sb.append("Стоимость производства: ").append(product.getManufactureCost()).append("\n");
            sb.append("Единица измерения: ").append(product.getUnitOfMeasure()).append("\n");

            if (product.getManufacturer() != null) {
                sb.append("Производитель:\n");
                sb.append("  Название: '").append(product.getManufacturer().getName()).append("'\n");
                sb.append("  Полное название: '").append(product.getManufacturer().getFullName()).append("'\n");
                sb.append("  Сотрудники: ").append(product.getManufacturer().getEmployeesCount()).append("\n");
            } else {
                sb.append("Производитель: не указан\n");
            }
            sb.append("----------------------------------------");

            return sb.toString();
        } catch (Exception e) {
            return "Ошибка при удалении: " + e.getMessage();
        }
    }

    @Override
    public String getDescription() {
        return "удалить и вывести первый элемент коллекции (с наименьшим manufactureCost)";
    }
}