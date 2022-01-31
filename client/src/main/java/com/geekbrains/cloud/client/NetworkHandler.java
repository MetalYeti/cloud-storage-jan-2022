package com.geekbrains.cloud.client;

import com.geekbrains.cloud.message.AbstractMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;

@Slf4j
public class NetworkHandler {

    private static CountDownLatch ready;
    private SocketChannel socketChannel;
    private static NetworkHandler instance;

    public static NetworkHandler getInstance() {
        if (instance == null) {
            instance = new NetworkHandler();
        }
        return instance;
    }

    public static NetworkHandler getInstanceAndSetHandlerCallback(Callback cb) {
        if (instance == null) {
            instance = new NetworkHandler();
        }
        try {
            ready.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        instance.setInboundHandlerCallback(cb);
        return instance;
    }

    public void setInboundHandlerCallback(Callback cb) {
        if (socketChannel != null && cb != null) {
            socketChannel.pipeline().get(ClientInboundHandler.class).setCallback(cb);
        }
    }

    private NetworkHandler() {
        ready = new CountDownLatch(1);
        Thread t = new Thread(() -> {

            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                Bootstrap b = new Bootstrap();
                b.group(workerGroup);
                b.channel(NioSocketChannel.class);
                b.handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        socketChannel = ch;
                        ch.pipeline().addLast(new ObjectEncoder(),
                                new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)),
                                new ClientInboundHandler()
                        );
                        ready.countDown();
                    }
                });
                ChannelFuture f = b.connect("localhost", 8189).sync();
                f.channel().closeFuture().sync();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                workerGroup.shutdownGracefully();
            }
        }
        );
        t.setDaemon(true);
        t.start();
    }

    public void sendMessage(AbstractMessage message) {
        if (socketChannel != null) {
            log.info("Sending message {}", message);
            socketChannel.writeAndFlush(message);
        }
    }
}

