package com.geekbrains.cloud.message;

import java.io.Serializable;

public class FileRequestMessage extends AbstractMessage implements Serializable {
    private String fileName;

    public FileRequestMessage(String fileName) {
        this.fileName = fileName;
        this.type = MessageType.FILE_REQUEST;
    }

    public String getFileName() {
        return fileName;
    }
}
