package org.example.commands;

import org.example.data.Product;
import org.example.data.Coordinates;
import org.example.data.Organization;
import org.example.data.UnitOfMeasure;
import org.example.data.User;
import org.example.management.CollectionManager;
import org.example.management.DatabaseManager;

import java.util.Arrays;

public class AddIfMinCommand implements Command {
    private final CollectionManager collectionManager;
    private final DatabaseManager databaseManager;

    public AddIfMinCommand(CollectionManager collectionManager, DatabaseManager databaseManager) {
        this.collectionManager = collectionManager;
        this.databaseManager = databaseManager;
    }

    @Override
    public String execute(String[] args, User user) {
        if (user == null) {
            return "Ошибка: требуется авторизация";
        }

        if (args.length < 10) {
            return "Недостаточно аргументов. Требуется: name, x, y, price, partNumber, manufactureCost, unitOfMeasure, orgName, orgFullName, employeesCount";
        }

        try {
            String name = args[0];
            if (name.isEmpty()) {
                return "Ошибка: название не может быть пустым";
            }

            long x = Long.parseLong(args[1]);
            if (x <= -349) {
                return "Ошибка: координата X должна быть больше -349";
            }

            float y = Float.parseFloat(args[2]);

            Long price = null;
            if (!args[3].isEmpty()) {
                price = Long.parseLong(args[3]);
                if (price <= 0) {
                    return "Ошибка: цена должна быть больше 0";
                }
            }

            String partNumber = args[4];
            if (partNumber.isEmpty()) {
                return "Ошибка: парт-номер не может быть пустым";
            }

            Float manufactureCost = null;
            if (!args[5].isEmpty()) {
                manufactureCost = Float.parseFloat(args[5]);
            }

            UnitOfMeasure unitOfMeasure;
            try {
                unitOfMeasure = UnitOfMeasure.valueOf(args[6].toUpperCase());
            } catch (IllegalArgumentException e) {
                return "Ошибка: недопустимая единица измерения. Допустимые значения: " + Arrays.toString(UnitOfMeasure.values());
            }

            String orgName = args[7];
            if (orgName.isEmpty()) {
                return "Ошибка: название организации не может быть пустым";
            }

            String orgFullName = args[8];
            if (orgFullName.isEmpty()) {
                return "Ошибка: полное название организации не может быть пустым";
            }

            long employeesCount = Long.parseLong(args[9]);
            if (employeesCount <= 0) {
                return "Ошибка: количество сотрудников должно быть больше 0";
            }

            // Создаем продукт
            Organization manufacturer = new Organization(orgName, orgFullName, employeesCount, user.getId());
            Product newProduct = new Product(
                    name,
                    new Coordinates(x, y),
                    price,
                    partNumber,
                    manufactureCost,
                    unitOfMeasure,
                    manufacturer,
                    user.getId()
            );

            // Сравниваем с минимальным элементом
            Product minProduct = collectionManager.getMin();
            if (minProduct == null || newProduct.compareTo(minProduct) < 0) {
                boolean success = collectionManager.addProduct(newProduct, user);
                if (success) {
                    return "Продукт добавлен. ID: " + newProduct.getId();
                } else {
                    return "Ошибка: не удалось добавить продукт в базу данных";
                }
            } else {
                return "Продукт НЕ добавлен: его значение не меньше минимального в коллекции.";
            }
        } catch (NumberFormatException e) {
            return "Ошибка: неверный формат числа";
        } catch (Exception e) {
            return "Ошибка при создании продукта: " + e.getMessage();
        }
    }

    @Override
    public String getDescription() {
        return "Добавить элемент, если он меньше минимального. Аргументы: name, x, y, price, partNumber, manufactureCost, unitOfMeasure, orgName, orgFullName, employeesCount";
    }
}