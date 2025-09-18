package org.example.network;

public class CommandRequest extends Request {
    private static final long serialVersionUID = 1L;
    private final String command;
    private final String[] args;
    private final String username;
    private final String passwordHash;

    public CommandRequest(String command, String[] args, String username, String passwordHash) {
        super("COMMAND");
        this.command = command;
        this.args = args;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public String getCommand() {
        return command;
    }

    public String[] getArgs() {
        return args;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    @Override
    public String toString() {
        return "CommandRequest{command='" + command + "', username='" + username +
                "', timestamp=" + getTimestamp() + "}";
    }
}