package org.example.commands;

import org.example.management.DatabaseManager;
import org.example.util.HashUtil;
import java.util.Scanner;

public class RegisterCommand implements Command {
    private final DatabaseManager dbManager;
    private final Scanner scanner;

    public RegisterCommand(DatabaseManager dbManager, Scanner scanner) {
        this.dbManager = dbManager;
        this.scanner = scanner;
    }

    @Override
    public String execute(String[] args, org.example.data.User user) {
        System.out.print("Введите логин: ");
        String username = scanner.nextLine().trim();

        System.out.print("Введите пароль: ");
        String password = scanner.nextLine().trim();

        try {
            if (username.isEmpty() || password.isEmpty()) {
                return "Логин и пароль не могут быть пустыми";
            }

            String passwordHash = HashUtil.sha256(password);
            dbManager.registerUser(username, passwordHash);
            return "Пользователь " + username + " успешно зарегистрирован";
        } catch (Exception e) {
            return "Ошибка: " + e.getMessage();
        }
    }

    @Override
    public String getDescription() {
        return "Зарегистрировать нового пользователя";
    }
}