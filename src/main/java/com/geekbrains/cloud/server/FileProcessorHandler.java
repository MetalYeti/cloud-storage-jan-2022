package com.geekbrains.cloud.server;

import com.geekbrains.cloud.client.FileWrapping;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class FileProcessorHandler extends ChannelInboundHandlerAdapter {

    private File currentDir;

    public FileProcessorHandler() throws IOException {
        currentDir = new File("serverDir");
    }

    public FileProcessorHandler(File currentDir) {
        this.currentDir = currentDir;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FileWrapping) {
            FileWrapping fw = (FileWrapping) msg;
            try (FileOutputStream fos = new FileOutputStream(currentDir.toPath().resolve(fw.getName()).toFile())) {
                fos.write(fw.getBytes());
            }
            log.info("File received {}", msg);
        } else if (msg instanceof String) {
            if (msg.equals("#GET#USER#FILES#")) {
                String[] currentDirFiles = currentDir.toPath().toFile().list();
                if (currentDirFiles != null) {
                    List<String> files = new ArrayList<>(Arrays.asList(currentDirFiles));
                    ctx.writeAndFlush(files);
                    log.info("List of files requested {}", msg);
                }
            } else if (((String) msg).startsWith("#GET#FILE#")) {
                String filename = ((String) msg).substring(10);
                File file = currentDir.toPath().resolve(filename).toFile();
                try (FileInputStream in = new FileInputStream(file)) {
                    byte[] bytes = new byte[(int) file.length()];
                    int read = in.read(bytes);
                    if (read > 0) {
                        FileWrapping fw = new FileWrapping(filename, bytes);
                        ctx.writeAndFlush(fw);
                    }

                }
                log.info("File requested {}", filename);
            }
        }
    }

    public void sendFile(String fileName) throws IOException {
//        File currentFile = currentDir.toPath().resolve(fileName).toFile();
//        os.writeUTF("#SEND#FILE#");
//        os.writeUTF(fileName);
//        os.writeLong(currentFile.length());
//        try (FileInputStream is = new FileInputStream(currentFile)) {
//            while (true) {
//                int read = is.read(buf);
//                if (read == -1) {
//                    break;
//                }
//                os.write(buf, 0, read);
//            }
//        }
//        os.flush();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("Client connected...");
    }

//    @Override
//    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
//        try {
//            log.info("Received: {}", s.trim());
////            if (s.equals("#SEND#FILE#")) {
////                String fileName = is.readUTF();
////                long size = is.readLong();
////                System.out.println("Created file: " + fileName);
////                System.out.println("File size: " + size);
////                Path currentPath = currentDir.toPath().resolve(fileName);
////                try (FileOutputStream fos = new FileOutputStream(currentPath.toFile())) {
////                    for (int i = 0; i < (size + SIZE - 1) / SIZE; i++) {
////                        int read = is.read(buf);
////                        fos.write(buf, 0, read);
////                    }
////                }
////                os.writeUTF("File successfully downloaded");
////                os.flush();
////            } else
//            if (s.trim().equals("#GET#USER#FILES#")) {
//                String[] currentDirFiles = currentDir.toPath().toFile().list();
//                if (currentDirFiles != null) {
//                    int filesCount = currentDirFiles.length;
//                    channelHandlerContext.writeAndFlush("#START#LIST#OF#" + filesCount);
//                    for (String item : currentDirFiles) {
//                        channelHandlerContext.writeAndFlush(item);
//                    }
//                    channelHandlerContext.writeAndFlush("#END#LIST#");
//                }
//            }
//            //else if (s.equals("#GET#FILE#")) {
////                String fileName = is.readUTF();
////                System.out.println("Requested file: " + fileName);
////                File currentFile = currentDir.toPath().resolve(fileName).toFile();
////                os.writeUTF("#GET#FILE#");
////                os.writeUTF(fileName);
////                os.writeLong(currentFile.length());
////                os.writeInt(SIZE);
////                try (FileInputStream fos = new FileInputStream(currentFile)) {
////                    while (true) {
////                        int read = fos.read(buf);
////                        if (read == -1) {
////                            break;
////                        }
////                        os.write(buf, 0, read);
////                    }
////                }
////                os.writeUTF("File successfully uploaded");
////                os.flush();
////            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
