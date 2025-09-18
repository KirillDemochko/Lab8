package org.example.commands;

import org.example.data.User;
import org.example.management.CollectionManager;

public class InfoCommand implements Command {
    private final CollectionManager collectionManager;

    public InfoCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public String execute(String[] args, User user) {
        return "Тип коллекции: " + collectionManager.getCollection().getClass().getName() + "\n" +
                "Дата инициализации: " + collectionManager.getInitTime() + "\n" +
                "Количество элементов: " + collectionManager.getCollection().size();
    }

    @Override
    public String getDescription() {
        return "Вывести информацию о коллекции";
    }
}