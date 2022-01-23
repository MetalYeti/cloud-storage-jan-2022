package com.geekbrains.cloud.client;

import java.io.Serializable;

public class FileWrapping implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private byte[] bytes;
    private long length;

    public FileWrapping(String name, byte[] bytes) {
        this.name = name;
        this.bytes = bytes;
        length = bytes.length;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
        length = bytes.length;
    }

    public long getLength() {
        return length;
    }

    @Override
    public String toString() {
        return "FileWrapping{" +
                "name='" + name + '\'' +
                ", length=" + length +
                '}';
    }
}
