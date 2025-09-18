package org.example;

import org.example.management.CollectionManager;
import org.example.management.CommandManager;
import org.example.management.DatabaseManager;
import org.example.server.Server;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        DatabaseManager databaseManager = new DatabaseManager();
        CollectionManager collectionManager = new CollectionManager(databaseManager);
        Scanner scanner = new Scanner(System.in);
        CommandManager commandManager = new CommandManager(collectionManager, databaseManager, scanner);
        int port = 5432;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Неверный формат порта. Используется порт по умолчанию: " + port);
            }
        }
        Server server = new Server(port, collectionManager, commandManager, databaseManager);
        server.start();
    }
}
