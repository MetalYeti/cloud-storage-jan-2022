package com.geekbrains.cloud.message;

public class AuthResponse extends AbstractMessage {
    private String response;
    private boolean authenticated;

    public AuthResponse(String response, boolean authenticated) {
        this.response = response;
        this.authenticated = authenticated;
        this.type = MessageType.AUTH_RESPONSE;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public String getResponse() {
        return response;
    }
}
