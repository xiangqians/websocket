package com.xq.ws;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author xiangqian
 */
public final class Session implements Closeable {

    private Channel channel;

    public Session(Channel channel) {
        this.channel = channel;
    }

    public boolean isActive() {
        if (this.channel == null) {
            return false;
        }
        if (!this.channel.isActive()) {
            this.channel = null;
            return false;
        }
        return true;
    }

    public void send(String text) {
        if (this.isActive()) {
            this.channel.writeAndFlush(new TextWebSocketFrame(text));
        }
    }

    public void send(byte[] bytes) {
        if (this.isActive()) {
            this.channel.writeAndFlush(new BinaryWebSocketFrame(Unpooled.buffer().writeBytes(bytes)));
        }
    }

    @Override
    public void close() throws IOException {
        if (this.isActive()) {
            this.channel.close();
            this.channel = null;
        }
    }
}
