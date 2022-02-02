package com.geekbrains.cloud.message;

public class ReadyMessage extends AbstractMessage{
    public ReadyMessage() {
        this.type = MessageType.READY;
    }
}
