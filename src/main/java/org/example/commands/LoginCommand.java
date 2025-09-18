package org.example.commands;

import org.example.data.User;
import org.example.management.DatabaseManager;
import org.example.management.CommandManager;
import org.example.util.HashUtil;
import java.util.Scanner;

public class LoginCommand implements Command {
    private final DatabaseManager dbManager;
    private final Scanner scanner;
    private final CommandManager cmdManager;

    public LoginCommand(DatabaseManager dbManager, Scanner scanner, CommandManager cmdManager) {
        this.dbManager = dbManager;
        this.scanner = scanner;
        this.cmdManager = cmdManager;
    }

    @Override
    public String execute(String[] args, User currentUser) {
        System.out.print("Введите логин: ");
        String username = scanner.nextLine().trim();

        System.out.print("Введите пароль: ");
        String password = scanner.nextLine().trim();

        try {
            User user = dbManager.authenticateUser(username, HashUtil.sha256(password));
            if (user != null) {
                // В серверной реализации это будет установлено в ClientHandler
                return "Авторизация успешна. Добро пожаловать, " + username;
            } else {
                return "Неверный логин или пароль";
            }
        } catch (Exception e) {
            return "Ошибка: " + e.getMessage();
        }
    }

    @Override
    public String getDescription() {
        return "Войти в систему";
    }
}