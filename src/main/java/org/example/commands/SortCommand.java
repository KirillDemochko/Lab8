package org.example.commands;

import org.example.data.User;
import org.example.management.CollectionManager;

public class SortCommand implements Command {
    private final CollectionManager collectionManager;

    public SortCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public String execute(String[] args, User user) {
        collectionManager.sort();
        return "Коллекция отсортирована в естественном порядке";
    }

    @Override
    public String getDescription() {
        return "отсортировать коллекцию в естественном порядке";
    }
}