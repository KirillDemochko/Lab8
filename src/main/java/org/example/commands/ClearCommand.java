package org.example.commands;

import org.example.data.User;
import org.example.management.CollectionManager;
import org.example.management.DatabaseManager;

public class ClearCommand implements Command {
    private final CollectionManager collectionManager;
    private final DatabaseManager databaseManager;

    public ClearCommand(CollectionManager collectionManager, DatabaseManager databaseManager) {
        this.collectionManager = collectionManager;
        this.databaseManager = databaseManager;
    }

    @Override
    public String execute(String[] args, User user) {
        if (user == null) {
            return "Ошибка: требуется авторизация";
        }

        try {
            collectionManager.clear(user);
            return "Все ваши продукты успешно удалены из коллекции";
        } catch (Exception e) {
            return "Ошибка при очистке коллекции: " + e.getMessage();
        }
    }

    @Override
    public String getDescription() {
        return "Очистить коллекцию (удалить все ваши продукты)";
    }
}