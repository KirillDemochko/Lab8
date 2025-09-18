package org.example.commands;

import org.example.data.Product;
import org.example.data.User;
import org.example.management.CollectionManager;

import java.util.List;

public class PrintAscendingCommand implements Command {
    private final CollectionManager collectionManager;

    public PrintAscendingCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public String execute(String[] args, User user) {
        List<Product> ascendingProducts = collectionManager.getAscending();

        if (ascendingProducts.isEmpty()) {
            return "Коллекция пуста";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Элементы коллекции в порядке возрастания:\n");
        sb.append("----------------------------------------\n");

        for (Product product : ascendingProducts) {
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
            sb.append("----------------------------------------\n");
        }

        return sb.toString();
    }

    @Override
    public String getDescription() {
        return "вывести элементы коллекции в порядке возрастания";
    }
}