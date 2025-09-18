package org.example.commands;

import org.example.data.Product;
import org.example.data.User;
import org.example.management.CollectionManager;

import java.util.List;

public class FilterByPriceCommand implements Command {
    private final CollectionManager collectionManager;

    public FilterByPriceCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public String execute(String[] args, User user) {
        if (args.length < 1) {
            return "Требуется аргумент: price";
        }

        try {
            Long price = Long.parseLong(args[0]);
            if (price <= 0) {
                return "Цена должна быть больше 0";
            }

            List<Product> filteredProducts = collectionManager.filterByPrice(price);

            if (filteredProducts.isEmpty()) {
                return "Нет продуктов с ценой " + price;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Элементы с price = ").append(price).append(":\n");
            sb.append("----------------------------------------\n");

            for (Product product : filteredProducts) {
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
        } catch (NumberFormatException e) {
            return "Ошибка: некорректное значение price";
        }
    }

    @Override
    public String getDescription() {
        return "вывести элементы, значение поля price которых равно заданному";
    }
}