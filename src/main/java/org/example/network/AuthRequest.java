package org.example.network;

public class AuthRequest extends Request {
    private static final long serialVersionUID = 1L;
    private final String username;
    private final String password;
    private boolean isRegistration = false;

    public AuthRequest(String username, String password) {
        super("AUTH");
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isRegistration() {
        return isRegistration;
    }

    public void setIsRegistration(boolean isRegistration) {
        this.isRegistration = isRegistration;
    }

    @Override
    public String toString() {
        return "AuthRequest{username='" + username + "', timestamp=" + getTimestamp() +
                ", isRegistration=" + isRegistration + "}";
    }
}