package com.geekbrains.cloud.message;

public class FileOperation extends AbstractMessage {
    private final String filename;
    private final String newFilename;
    private final long size;

    public FileOperation(String filename) {
        this(filename, 0);
    }

    public FileOperation(String filename, long size) {
        this(filename, size, MessageType.FILE_INFO);
    }

    public FileOperation(String filename, String newFilename){
        this.filename = filename;
        this.newFilename = newFilename;
        this.size = 0;
        this.type = MessageType.RENAME;
    }

    public FileOperation(String filename,  MessageType type) {
        this(filename, 0, type);
    }

    public FileOperation(String filename, long size, MessageType type) {
        if (filename.startsWith("[")) {
            this.filename = filename.substring(1, filename.length() - 1);
        } else {
            this.filename = filename;
        }
        this.newFilename = "";
        this.size = size;
        this.type = type;
    }

    public String getFilename() {
        return filename;
    }

    public String getNewFilename() {
        return newFilename;
    }

    public long getSize() {
        return size;
    }
}
