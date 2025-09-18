package org.example.commands;

import org.example.data.User;
import org.example.management.CommandManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class ExecuteScriptCommand implements Command {
    private final CommandManager commandManager;
    private static final Set<Path> executingScripts = new HashSet<>();

    public ExecuteScriptCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public String execute(String[] args, User user) {
        if (user == null) {
            return "Ошибка: требуется авторизация";
        }

        if (args.length < 1) {
            return "Ошибка: укажите имя файла скрипта";
        }

        try {
            Path scriptPath = Paths.get(args[0]).toAbsolutePath().normalize();

            // Проверяем существование файла
            if (!Files.exists(scriptPath)) {
                return "Файл не существует: " + scriptPath;
            }
            if (!Files.isReadable(scriptPath)) {
                return "Ошибка: нет прав на чтение файла";
            }
            if (Files.isDirectory(scriptPath)) {
                return "Ошибка: указанный путь ведет к директории";
            }

            // Проверка на рекурсию
            if (executingScripts.contains(scriptPath)) {
                return "Ошибка: обнаружена рекурсия! Скрипт " + scriptPath + " уже выполняется";
            }

            executingScripts.add(scriptPath);
            StringBuilder result = new StringBuilder();

            try (BufferedReader reader = Files.newBufferedReader(scriptPath)) {
                result.append("Выполнение скрипта: ").append(scriptPath).append("\n");
                result.append(executeScriptLines(reader, scriptPath, user));
            } finally {
                executingScripts.remove(scriptPath);
            }

            return result.toString();
        } catch (Exception e) {
            return "Ошибка выполнения скрипта: " + e.getMessage();
        }
    }

    private String executeScriptLines(BufferedReader reader, Path scriptPath, User user) {
        StringBuilder result = new StringBuilder();
        String line;
        int lineNumber = 0;

        try {
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("//")) continue;

                try {
                    String[] parts = line.split("\\s+", 2);
                    String commandName = parts[0];
                    String[] commandArgs = parts.length > 1 ? parts[1].split("\\s+") : new String[0];

                    // Пропускаем команды execute_script и save внутри скрипта
                    if (commandName.equals("execute_script") || commandName.equals("save")) {
                        result.append("Пропуск команды ").append(commandName).append(" в скрипте\n");
                        continue;
                    }

                    result.append("[").append(scriptPath.getFileName()).append(":").append(lineNumber).append("] ")
                            .append(line).append("\n");

                    // Обработка специальных команд
                    if (commandName.equals("add") || commandName.equals("add_if_min")) {
                        if (commandArgs.length < 10) {
                            result.append("Ошибка: недостаточно аргументов для команды ").append(commandName).append("\n");
                            continue;
                        }
                    } else if (commandName.equals("update")) {
                        if (commandArgs.length < 11) {
                            result.append("Ошибка: недостаточно аргументов для команды update\n");
                            continue;
                        }
                    }

                    String commandResult = commandManager.executeCommand(commandName, commandArgs, user);
                    result.append(commandResult).append("\n");
                } catch (Exception e) {
                    result.append("Ошибка в строке ").append(lineNumber).append(": ").append(e.getMessage()).append("\n");
                }
            }
        } catch (IOException e) {
            result.append("Ошибка чтения файла: ").append(e.getMessage());
        }

        return result.toString();
    }

    @Override
    public String getDescription() {
        return "execute_script file_name : выполнить скрипт из указанного файла (не поддерживает вложенные execute_script и save)";
    }
}