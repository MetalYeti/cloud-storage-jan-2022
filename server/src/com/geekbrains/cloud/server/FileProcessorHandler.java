package com.geekbrains.cloud.server;

import com.geekbrains.cloud.message.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class FileProcessorHandler extends ChannelInboundHandlerAdapter {

    private File currentDir;
    private File rootDir;

    private AuthService auth = new SimpleAuthService();

    public FileProcessorHandler() throws IOException {
        currentDir = new File("serverDir");
    }

    public FileProcessorHandler(File currentDir) throws IOException {
        boolean root = false;
        if (!currentDir.exists()) {
            root = currentDir.mkdir();
        }
        if (root) {
            this.currentDir = currentDir;
        } else {
            throw new RuntimeException("Cannot create root server directory " + currentDir);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        switch (((AbstractMessage) msg).getType()) {
            case READY:
                sendFilesList(ctx);
                break;
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
                String filename = ((FileOperation) msg).getFilename();
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
                        rootDir = currentDir;
                        if (!currentDir.exists()) {
                            currentDir.mkdir();
                        }
                        ctx.writeAndFlush(new AuthResponse("Welcome " + user, true));
                    } else {
                        ctx.writeAndFlush(new AuthResponse("Wrong password", false));
                    }
                } else {
                    ctx.writeAndFlush(new AuthResponse("User does`t exist", false));
                }
                log.info("Auth attempt from {}", user);
                break;
            case AUTH_RESPONSE:
                break;
            case FILE_INFO:
                FileOperation fi = (FileOperation) msg;

                File target;
                if (fi.getFilename().equals("..")) {
                    if (currentDir.equals(rootDir)) {
                        target = rootDir;
                    } else {
                        target = currentDir.toPath().getParent().toFile();
                    }
                } else {
                    target = currentDir.toPath().resolve(fi.getFilename()).toFile();
                }
                if (target.isDirectory()) {
                    currentDir = target;
                    sendFilesList(ctx);
                    log.info("Navigating to {}", currentDir);
                } else {
                    ctx.writeAndFlush(new FileOperation(fi.getFilename(), target.length()));
                    log.info("File info requested {}", msg);
                }
                break;
            case NEW_FOLDER:
                FileOperation newFolder = (FileOperation) msg;
                File folder = currentDir.toPath().resolve(newFolder.getFilename()).toFile();
                boolean created = false;
                if (!folder.exists()) {
                    created = folder.mkdir();
                    log.info("Creating folder {}", newFolder.getFilename());
                }

                if (created) {
                    sendFilesList(ctx);
                }
                break;
            case DELETE:
                FileOperation del = (FileOperation) msg;
                boolean deleted = currentDir.toPath().resolve(del.getFilename()).toFile().delete();
                log.info("Deleting {}", del.getFilename());
                if (deleted) {
                    sendFilesList(ctx);
                }
                break;
            case RENAME:
                FileOperation rename = (FileOperation) msg;
                File oldName = currentDir.toPath().resolve(rename.getFilename()).toFile();
                File newName = currentDir.toPath().resolve(rename.getNewFilename()).toFile();
                boolean renamed = oldName.renameTo(newName);
                log.info("Renaming {} -> {}", rename.getFilename(), rename.getNewFilename());
                if (renamed) {
                    sendFilesList(ctx);
                }
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
            List<String> files = Arrays.stream(currentDirFiles).map(e -> Paths.get(e).toFile().isDirectory() ? "[" + e + "]" : e).sorted(Comparator.naturalOrder()).collect(Collectors.toList());
            log.info(files.toString());
            ctx.writeAndFlush(new FileList(files));
        }
    }
}
