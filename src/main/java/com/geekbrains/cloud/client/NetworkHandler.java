package com.geekbrains.cloud.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NetworkHandler {
    SocketChannel socketChannel;

    public NetworkHandler(Callback cb) {
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
                                new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        if (cb != null) {
                                            cb.action(ctx, msg);
                                        }
                                    }
                                });
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

    public void sendMessage(String message) {
        if (socketChannel != null) {
            log.info("Sending message {}", message);
            socketChannel.writeAndFlush(message);
        }
    }

    public void sendFile(FileWrapping file) {
        socketChannel.writeAndFlush(file);
    }
}

