package org.example.commands;

import org.example.data.Product;
import org.example.data.User;
import org.example.management.CollectionManager;
import org.example.management.DatabaseManager;

public class RemoveByIdCommand implements Command {
    private final CollectionManager collectionManager;
    private final DatabaseManager databaseManager;

    public RemoveByIdCommand(CollectionManager collectionManager, DatabaseManager databaseManager) {
        this.collectionManager = collectionManager;
        this.databaseManager = databaseManager;
    }

    @Override
    public String execute(String[] args, User user) {
        if (user == null) {
            return "Ошибка: требуется авторизация";
        }

        try {
            if (args.length < 1) {
                return "Требуется аргумент: ID продукта";
            }

            long id = Long.parseLong(args[0]); // Изменено на long

            // Проверяем существование продукта и права доступа
            Product product = collectionManager.getById(id);
            if (product == null) {
                return "Продукт с ID " + id + " не найден";
            }

            if (product.getCreatorId() != user.getId()) {
                return "Ошибка: вы можете удалять только свои продукты";
            }

            boolean success = collectionManager.removeById(id, user);
            if (success) {
                return "Продукт с ID " + id + " удален.";
            } else {
                return "Ошибка: не удалось удалить продукт";
            }
        } catch (NumberFormatException e) {
            return "Ошибка: некорректный формат ID";
        } catch (Exception e) {
            return "Ошибка: " + e.getMessage();
        }
    }

    @Override
    public String getDescription() {
        return "Удалить элемент по ID";
    }
}