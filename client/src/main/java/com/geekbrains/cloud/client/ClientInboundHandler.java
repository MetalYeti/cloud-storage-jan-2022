package com.geekbrains.cloud.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientInboundHandler extends ChannelInboundHandlerAdapter {

    private Callback callback;

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info(msg.toString());
        if (callback != null) {
            callback.action(ctx, msg);
        } else {
            log.warn("InboundHandlerCallback is not set! All incoming messages discarded!");
        }
    }
}

