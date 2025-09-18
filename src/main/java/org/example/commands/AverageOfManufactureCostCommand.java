package org.example.commands;

import org.example.data.User;
import org.example.management.CollectionManager;

public class AverageOfManufactureCostCommand implements Command {
    private final CollectionManager collectionManager;

    public AverageOfManufactureCostCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public String execute(String[] args, User user) {
        double avg = collectionManager.getAverageManufactureCost();
        return String.format("Средняя стоимость производства: %.2f", avg);
    }

    @Override
    public String getDescription() {
        return "Вывести среднее значение manufactureCost";
    }
}