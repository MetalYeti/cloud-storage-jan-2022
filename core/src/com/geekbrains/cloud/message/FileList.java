package com.geekbrains.cloud.message;

import java.io.Serializable;
import java.util.List;

public class FileList extends AbstractMessage implements Serializable {
    private List<String> list;

    public FileList(List<String> list) {
        this.list = list;
        this.type = MessageType.FILE_LIST;
    }

    public List<String> getList() {
        return list;
    }
}
