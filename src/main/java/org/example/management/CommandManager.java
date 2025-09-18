package org.example.management;

import org.example.commands.*;
import org.example.data.User;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CommandManager {
    private final Map<String, Command> commands = new HashMap<>();
    private final CollectionManager collectionManager;
    private final DatabaseManager databaseManager;
    private final Scanner scanner;

    public CommandManager(CollectionManager collectionManager, DatabaseManager databaseManager, Scanner scanner) {
        this.collectionManager = collectionManager;
        this.databaseManager = databaseManager;
        this.scanner = scanner;
        registerCommands();
    }

    private void registerCommands() {
        // Регистрируем все команды, кроме help
        commands.put("info", new InfoCommand(collectionManager));
        commands.put("show", new ShowCommand(collectionManager));
        commands.put("add", new AddCommand(collectionManager, databaseManager));
        commands.put("update", new UpdateCommand(collectionManager, databaseManager));
        commands.put("add_if_min", new AddIfMinCommand(collectionManager, databaseManager));
        commands.put("remove_by_id", new RemoveByIdCommand(collectionManager, databaseManager));
        commands.put("clear", new ClearCommand(collectionManager, databaseManager));
        commands.put("execute_script", new ExecuteScriptCommand(this));
        commands.put("exit", new ExitCommand());
        commands.put("head", new HeadCommand(collectionManager));
        commands.put("remove_head", new RemoveHeadCommand(collectionManager, databaseManager));
        commands.put("average_of_manufacture_cost", new AverageOfManufactureCostCommand(collectionManager));
        commands.put("sort", new SortCommand(collectionManager));
        commands.put("history", new HistoryCommand());
        commands.put("filter_by_price", new FilterByPriceCommand(collectionManager));
        commands.put("print_ascending", new PrintAscendingCommand(collectionManager));
        commands.put("print_field_ascending_price", new PrintFieldAscendingPriceCommand(collectionManager));

        // Команды аутентификации
        commands.put("register", new RegisterCommand(databaseManager, scanner));
        commands.put("login", new LoginCommand(databaseManager, scanner, this));

        // Теперь регистрируем help
        commands.put("help", new HelpCommand(this));
    }

    public String executeCommand(String commandName, String[] args, User user) throws Exception {
        Command cmd = commands.get(commandName.toLowerCase());
        if (cmd == null) {
            throw new IllegalArgumentException("Неизвестная команда: " + commandName);
        }

        if (!commandName.equalsIgnoreCase("history")) {
            HistoryCommand.addToHistory(commandName);
        }

        return cmd.execute(args, user);
    }

    public Map<String, Command> getCommands() {
        return new HashMap<>(commands);
    }
}