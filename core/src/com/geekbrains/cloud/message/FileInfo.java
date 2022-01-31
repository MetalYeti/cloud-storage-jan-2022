package com.geekbrains.cloud.message;

public class FileInfo extends AbstractMessage{
    private final String filename;
    private final long size;

    public FileInfo(String filename) {
        this(filename, 0);
    }

    public FileInfo(String filename, long size) {
        this.filename = filename;
        this.size = size;
        this.type = MessageType.FILE_INFO;
    }

    public String getFilename() {
        return filename;
    }

    public long getSize() {
        return size;
    }
}
