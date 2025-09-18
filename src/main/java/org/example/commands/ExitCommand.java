package org.example.commands;

import org.example.data.User;

public class ExitCommand implements Command {
    @Override
    public String execute(String[] args, User user) {
        return "Завершение работы клиента.";
    }

    @Override
    public String getDescription() {
        return "Завершить работу клиента";
    }
}