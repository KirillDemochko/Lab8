package org.example.commands;

import org.example.data.User;
import org.example.management.CollectionManager;

import java.util.List;

public class PrintFieldAscendingPriceCommand implements Command {
    private final CollectionManager collectionManager;

    public PrintFieldAscendingPriceCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public String execute(String[] args, User user) {
        List<Long> ascendingPrices = collectionManager.getAscendingPrices();

        if (ascendingPrices.isEmpty()) {
            return "В коллекции нет продуктов с указанной ценой";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Значения поля price в порядке возрастания:\n");
        sb.append("----------------------------------------\n");

        for (Long price : ascendingPrices) {
            sb.append(price).append("\n");
        }
        sb.append("----------------------------------------");

        return sb.toString();
    }

    @Override
    public String getDescription() {
        return "вывести значения поля price всех элементов в порядке возрастания";
    }
}