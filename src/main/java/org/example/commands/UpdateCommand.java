package org.example.commands;

import org.example.data.Product;
import org.example.data.Coordinates;
import org.example.data.Organization;
import org.example.data.UnitOfMeasure;
import org.example.data.User;
import org.example.management.CollectionManager;
import org.example.management.DatabaseManager;

import java.util.Arrays;

public class UpdateCommand implements Command {
    private final CollectionManager collectionManager;
    private final DatabaseManager databaseManager;

    public UpdateCommand(CollectionManager collectionManager, DatabaseManager databaseManager) {
        this.collectionManager = collectionManager;
        this.databaseManager = databaseManager;
    }

    @Override
    public String execute(String[] args, User user) {
        if (user == null) {
            return "Ошибка: требуется авторизация";
        }

        if (args.length < 11) {
            return "Недостаточно аргументов. Требуется: id, name, x, y, price, partNumber, manufactureCost, unitOfMeasure, orgName, orgFullName, employeesCount";
        }

        try {
            long id = Long.parseLong(args[0]);

            // Проверяем существование продукта и права доступа
            Product oldProduct = collectionManager.getById(id);
            if (oldProduct == null) {
                return "Продукт с ID " + id + " не найден";
            }

            if (oldProduct.getCreatorId() != user.getId()) {
                return "Ошибка: вы можете изменять только свои продукты";
            }

            String name = args[1];
            if (name.isEmpty()) {
                return "Ошибка: название не может быть пустым";
            }

            long x = Long.parseLong(args[2]);
            if (x <= -349) {
                return "Ошибка: координата X должна быть больше -349";
            }

            float y = Float.parseFloat(args[3]);

            Long price = null;
            if (!args[4].isEmpty()) {
                price = Long.parseLong(args[4]);
                if (price <= 0) {
                    return "Ошибка: цена должна быть больше 0";
                }
            }

            String partNumber = args[5];
            if (partNumber.isEmpty()) {
                return "Ошибка: парт-номер не может быть пустым";
            }

            Float manufactureCost = null;
            if (!args[6].isEmpty()) {
                manufactureCost = Float.parseFloat(args[6]);
            }

            UnitOfMeasure unitOfMeasure;
            try {
                unitOfMeasure = UnitOfMeasure.valueOf(args[7].toUpperCase());
            } catch (IllegalArgumentException e) {
                return "Ошибка: недопустимая единица измерения. Допустимые значения: " + Arrays.toString(UnitOfMeasure.values());
            }

            String orgName = args[8];
            if (orgName.isEmpty()) {
                return "Ошибка: название организации не может быть пустым";
            }

            String orgFullName = args[9];
            if (orgFullName.isEmpty()) {
                return "Ошибка: полное название организации не может быть пустым";
            }

            long employeesCount = Long.parseLong(args[10]);
            if (employeesCount <= 0) {
                return "Ошибка: количество сотрудников должно быть больше 0";
            }

            // Создаем обновленный продукт
            Organization manufacturer = new Organization(orgName, orgFullName, employeesCount, user.getId());
            Product updatedProduct = new Product(
                    name,
                    new Coordinates(x, y),
                    price,
                    partNumber,
                    manufactureCost,
                    unitOfMeasure,
                    manufacturer,
                    user.getId()
            );

            // Обновляем продукт
            boolean success = collectionManager.updateProduct(id, updatedProduct, user);
            if (success) {
                return "Продукт с ID " + id + " успешно обновлен";
            } else {
                return "Ошибка: не удалось обновить продукт";
            }
        } catch (NumberFormatException e) {
            return "Ошибка: неверный формат числа";
        } catch (Exception e) {
            return "Ошибка при обновлении продукта: " + e.getMessage();
        }
    }

    @Override
    public String getDescription() {
        return "Обновить элемент по ID. Аргументы: id, name, x, y, price, partNumber, manufactureCost, unitOfMeasure, orgName, orgFullName, employeesCount";
    }
}