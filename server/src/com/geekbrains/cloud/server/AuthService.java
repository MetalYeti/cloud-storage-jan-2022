package com.geekbrains.cloud.server;

public interface AuthService {
    boolean authorize(String user, String pass);

    boolean userExists(String user);

    boolean registerUser(String user, String pass);
}
