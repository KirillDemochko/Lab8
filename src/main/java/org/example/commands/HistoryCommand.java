package org.example.commands;

import org.example.data.User;
import java.util.LinkedList;
import java.util.Queue;

public class HistoryCommand implements Command {
    private static final int MAX_HISTORY_SIZE = 15;
    private static final Queue<String> commandHistory = new LinkedList<>();

    public static void addToHistory(String commandName) {
        if (commandHistory.size() >= MAX_HISTORY_SIZE) {
            commandHistory.poll();
        }
        commandHistory.add(commandName);
    }

    @Override
    public String execute(String[] args, User user) {
        if (commandHistory.isEmpty()) {
            return "История команд пуста";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Последние ").append(commandHistory.size()).append(" команд:\n");
        int i = 1;
        for (String command : commandHistory) {
            sb.append(i++).append(". ").append(command).append("\n");
        }
        return sb.toString();
    }

    @Override
    public String getDescription() {
        return "вывести последние 15 команд (без их аргументов)";
    }
}