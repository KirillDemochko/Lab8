package org.example.commands;

import org.example.data.User;

public interface Command {
    String execute(String[] args, User user) throws Exception;
    String getDescription();
}