package com.geekbrains.cloud.message;

public class AuthRequest extends AbstractMessage {
    private final String username;
    private final String password;

    public AuthRequest(String username, String password) {
        this.username = username;
        this.password = password;
        this.type = MessageType.AUTH_REQUEST;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
