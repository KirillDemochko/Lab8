package org.example.commands;

import org.example.data.Product;
import org.example.data.User;
import org.example.management.CollectionManager;
import java.util.Objects;

public class FilterByManufactureCostCommand implements Command {
    private final CollectionManager collectionManager;

    public FilterByManufactureCostCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public String execute(String[] args, User user) {
        try {
            if (args.length < 1) {
                return "Требуется аргумент: manufactureCost";
            }
            Float cost = Float.parseFloat(args[0]);

            StringBuilder sb = new StringBuilder();
            sb.append("Элементы с manufactureCost = ").append(cost).append(":\n");
            sb.append("----------------------------------------\n");

            collectionManager.getCollection().stream()
                    .filter(p -> Objects.equals(cost, p.getManufactureCost()))
                    .forEach(product -> sb.append(printProductDetails(product)));

            sb.append("----------------------------------------");
            return sb.toString();
        } catch (NumberFormatException e) {
            return "Ошибка: некорректное значение manufactureCost";
        }
    }

    private String printProductDetails(Product product) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(product.getId()).append("\n");
        sb.append("Название: ").append(product.getName()).append("\n");
        sb.append("Координаты: (").append(product.getCoordinates().getX()).append(", ")
                .append(product.getCoordinates().getY()).append(")\n");
        sb.append("Дата создания: ").append(product.getCreationDate()).append("\n");
        sb.append("Цена: ").append(product.getPrice()).append("\n");
        sb.append("Парт-номер: ").append(product.getPartNumber()).append("\n");
        sb.append("Стоимость производства: ").append(product.getManufactureCost()).append("\n");
        sb.append("Единица измерения: ").append(product.getUnitOfMeasure()).append("\n");

        if (product.getManufacturer() != null) {
            sb.append("Производитель:\n");
            sb.append("  Название: ").append(product.getManufacturer().getName()).append("\n");
            sb.append("  Полное название: ").append(product.getManufacturer().getFullName()).append("\n");
            sb.append("  Сотрудники: ").append(product.getManufacturer().getEmployeesCount()).append("\n");
        } else {
            sb.append("Производитель: не указан\n");
        }
        sb.append("----------------------------------------\n");
        return sb.toString();
    }

    @Override
    public String getDescription() {
        return "вывести элементы с указанной manufactureCost (полная информация)";
    }
}