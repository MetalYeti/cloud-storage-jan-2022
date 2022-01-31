package com.geekbrains.cloud.server;

import com.geekbrains.cloud.message.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
public class FileProcessorHandler extends ChannelInboundHandlerAdapter {

    private File currentDir;

    private AuthService auth = new SimpleAuthService();

    public FileProcessorHandler() throws IOException {
        currentDir = new File("serverDir");
    }

    public FileProcessorHandler(File currentDir) {
        this.currentDir = currentDir;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        switch (((AbstractMessage) msg).getType()) {
            case FILE_MESSAGE:
                FileWrapping fw = (FileWrapping) msg;
                try (FileOutputStream fos = new FileOutputStream(currentDir.toPath().resolve(fw.getName()).toFile())) {
                    fos.write(fw.getBytes());
                }
                sendFilesList(ctx);
                log.info("File received {}", msg);
                break;
            case FILE_LIST:
                sendFilesList(ctx);
                log.info("List of files requested {}", msg);
                break;
            case FILE_REQUEST:
                String filename = ((FileRequestMessage) msg).getFileName();
                File file = currentDir.toPath().resolve(filename).toFile();
                try (FileInputStream in = new FileInputStream(file)) {
                    byte[] bytes = new byte[(int) file.length()];
                    int read = in.read(bytes);
                    if (read > 0) {
                        ctx.writeAndFlush(new FileWrapping(filename, bytes));
                    }
                }
                log.info("File requested {}", filename);
                break;
            case AUTH_REQUEST:
                String user = ((AuthRequest) msg).getUsername();
                String pass = ((AuthRequest) msg).getPassword();

                if (auth.userExists(user)) {
                    if (auth.authorize(user, pass)) {
                        currentDir = currentDir.toPath().resolve(user).toFile();
                        if (!currentDir.exists()){
                            currentDir.mkdir();
                        }
                        ctx.writeAndFlush(new AuthResponse("Welcome " + user, true));
                        sendFilesList(ctx);
                    } else {
                        ctx.writeAndFlush(new AuthResponse("Wrong password", false));
                    }
                } else {
                    ctx.writeAndFlush(new AuthResponse("User does`t exist", false));
                }
                log.info("Auth attempt from {}", user);
                break;
            case FILE_INFO:
                FileInfo fi = (FileInfo) msg;
                File target = currentDir.toPath().resolve(fi.getFilename()).toFile();
                ctx.writeAndFlush(new FileInfo(fi.getFilename(), target.length()));
                log.info("File info requested {}", msg);
                break;
        }
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("Client connected...");
    }

    private void sendFilesList(ChannelHandlerContext ctx) {
        String[] currentDirFiles = currentDir.toPath().toFile().list();
        if (currentDirFiles != null) {
            List<String> files = new ArrayList<>(Arrays.asList(currentDirFiles));
            ctx.writeAndFlush(new FileList(files));
        }
    }
}
