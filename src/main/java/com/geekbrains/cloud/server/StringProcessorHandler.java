package com.geekbrains.cloud.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class StringProcessorHandler extends SimpleChannelInboundHandler<String> {

    private File currentDir;
    public StringProcessorHandler(File currentDir) {
        this.currentDir = currentDir;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        log.info(msg);
        if (msg.trim().equals("#GET#USER#FILES#")) {
            String[] currentDirFiles = currentDir.toPath().toFile().list();
            if (currentDirFiles != null) {
                int filesCount = currentDirFiles.length;
                ctx.writeAndFlush("#START#LIST#OF#" + filesCount);
                for (String item : currentDirFiles) {
                    ctx.writeAndFlush(item);
                }
                ctx.writeAndFlush("#END#LIST#");
            }
        }
    }
}
