package com.geekbrains.cloud.server;

import java.util.HashMap;
import java.util.Map;

public class SimpleAuthService implements AuthService {

    private Map<String, String> users;

    public SimpleAuthService() {
        this.users = new HashMap<>();
        users.put("Alex", "qwer");
        users.put("Vasya", "asdf");
        users.put("Peter", "zxcv");
        users.put("Gena", "1234");
    }

    @Override
    public boolean authorize(String user, String pass) {
        return userExists(user) && users.get(user).equals(pass);
    }

    @Override
    public boolean userExists(String user) {
        return users.containsKey(user);
    }

    @Override
    public boolean registerUser(String user, String pass) {
        if (!userExists(user)){
            users.put(user, pass);
            return true;
        }
        return false;
    }
}
