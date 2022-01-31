package com.geekbrains.cloud.client;

import java.io.IOException;

public interface Callback {
    void action(Object... args) throws IOException;
}
