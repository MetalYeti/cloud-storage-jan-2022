package com.geekbrains.cloud.message;

import java.io.Serializable;

public abstract class AbstractMessage implements Serializable {
    protected MessageType type;

    public MessageType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "AbstractMessage{" +
                "type=" + type +
                '}';
    }
}
