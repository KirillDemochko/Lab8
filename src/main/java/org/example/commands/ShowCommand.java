package org.example.commands;

import org.example.data.Product;
import org.example.data.User;
import org.example.management.CollectionManager;

import java.util.PriorityQueue;

public class ShowCommand implements Command {
    private final CollectionManager collectionManager;

    public ShowCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public String execute(String[] args, User user) {
        if (collectionManager.getCollection().isEmpty()) {
            return "Коллекция пуста.";
        }

        StringBuilder sb = new StringBuilder();
        // Создаем копию, чтобы не нарушать основную коллекцию
        PriorityQueue<Product> copy = new PriorityQueue<>(collectionManager.getCollection());

        while (!copy.isEmpty()) {
            Product p = copy.poll();
            sb.append("ID: ").append(p.getId()).append("\n");
            sb.append("Название: ").append(p.getName()).append("\n");
            sb.append("Координаты: (").append(p.getCoordinates().getX()).append(", ")
                    .append(p.getCoordinates().getY()).append(")\n");
            sb.append("Дата создания: ").append(p.getCreationDate()).append("\n");
            sb.append("Цена: ").append(p.getPrice()).append("\n");
            sb.append("Парт-номер: ").append(p.getPartNumber()).append("\n");
            sb.append("Стоимость производства: ").append(p.getManufactureCost()).append("\n");
            sb.append("Единица измерения: ").append(p.getUnitOfMeasure()).append("\n");

            if (p.getManufacturer() != null) {
                sb.append("Производитель:\n");
                sb.append("  Название: ").append(p.getManufacturer().getName()).append("\n");
                sb.append("  Полное название: ").append(p.getManufacturer().getFullName()).append("\n");
                sb.append("  Сотрудники: ").append(p.getManufacturer().getEmployeesCount()).append("\n");
            } else {
                sb.append("Производитель: не указан\n");
            }
            sb.append("----------------------\n");
        }

        return sb.toString();
    }

    @Override
    public String getDescription() {
        return "вывести все элементы коллекции с полной информацией";
    }
}