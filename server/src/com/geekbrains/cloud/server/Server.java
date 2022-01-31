package com.geekbrains.cloud.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

@Slf4j
public class Server {

    private static File currentDir;

    public static void main(String[] args) throws IOException {
        currentDir = new File("serverDir");
        EventLoopGroup connGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap.group(connGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ClientHandlerPipeline(currentDir));

            ChannelFuture f = serverBootstrap.bind(8189).sync();
            log.info("Server started...");

            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            connGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
