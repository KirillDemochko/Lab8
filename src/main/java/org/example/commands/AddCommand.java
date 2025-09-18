package org.example.commands;

import org.example.data.Product;
import org.example.data.Coordinates;
import org.example.data.Organization;
import org.example.data.UnitOfMeasure;
import org.example.data.User;
import org.example.management.CollectionManager;
import org.example.management.DatabaseManager;

import java.util.Arrays;

public class AddCommand implements Command {
    private final CollectionManager collectionManager;
    private final DatabaseManager databaseManager;

    public AddCommand(CollectionManager collectionManager, DatabaseManager databaseManager) {
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
            long x = Long.parseLong(args[1]);
            float y = Float.parseFloat(args[2]);
            Long price = args[3].isEmpty() ? null : Long.parseLong(args[3]);
            String partNumber = args[4];
            Float manufactureCost = args[5].isEmpty() ? null : Float.parseFloat(args[5]);
            UnitOfMeasure unitOfMeasure = UnitOfMeasure.valueOf(args[6].toUpperCase());
            String orgName = args[7];
            String orgFullName = args[8];
            long employeesCount = Long.parseLong(args[9]);

            // Валидация данных
            if (x <= -349) {
                return "Ошибка: координата X должна быть больше -349";
            }
            if (price != null && price <= 0) {
                return "Ошибка: цена должна быть больше 0";
            }
            if (employeesCount <= 0) {
                return "Ошибка: количество сотрудников должно быть больше 0";
            }

            Organization manufacturer = new Organization(orgName, orgFullName, employeesCount, user.getId());
            Product product = new Product(name, new Coordinates(x, y), price, partNumber, manufactureCost, unitOfMeasure, manufacturer, user.getId());

            boolean success = collectionManager.addProduct(product, user);
            if (success) {
                return "Продукт успешно добавлен с ID: " + product.getId();
            } else {
                return "Ошибка: не удалось добавить продукт в базу данных";
            }
        } catch (NumberFormatException e) {
            return "Ошибка: неверный формат числа";
        } catch (IllegalArgumentException e) {
            return "Ошибка: неверное значение для unitOfMeasure. Допустимые значения: " + Arrays.toString(UnitOfMeasure.values());
        } catch (Exception e) {
            return "Ошибка при создании продукта: " + e.getMessage();
        }
    }

    @Override
    public String getDescription() {
        return "Добавить новый элемент в коллекцию. Аргументы: name, x, y, price, partNumber, manufactureCost, unitOfMeasure, orgName, orgFullName, employeesCount";
    }
}