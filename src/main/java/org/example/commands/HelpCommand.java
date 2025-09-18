// HelpCommand.java
package org.example.commands;

import org.example.data.User;
import org.example.management.CommandManager;
import java.util.Map;

public class HelpCommand implements Command {
    private final CommandManager commandManager;

    public HelpCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public String execute(String[] args, User user) {
        Map<String, Command> commands = commandManager.getCommands();
        StringBuilder sb = new StringBuilder();
        sb.append("Доступные команды:\n");
        commands.forEach((name, cmd) ->
                sb.append(String.format("%-30s %s\n", name, cmd.getDescription()))
        );
        return sb.toString();
    }

    @Override
    public String getDescription() {
        return "Вывести справку по доступным командам";
    }
}