package com.geekbrains.cloud.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.io.File;

public class ClientHandlerPipeline extends ChannelInitializer<SocketChannel> {

    private File currentDir;

    public ClientHandlerPipeline(File currentDir) {
        this.currentDir = currentDir;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline().addLast(
                new ObjectEncoder(),
                new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)),
                new FileProcessorHandler(currentDir));
    }
}
