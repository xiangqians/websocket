package com.xiangqian.ws.server;

import com.xiangqian.ws.server.config.ServiceChannel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xiangqian
 * @date 14:00 2020/01/11
 */
@Slf4j
public class WSFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("新连接加入");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame webSocketFrame) throws Exception {
        log.debug("读取channel信息");

        // 判断是否ping消息
        if (webSocketFrame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(webSocketFrame.content().retain()));
            return;
        }

        // 判断是否关闭链路的指令
        if (webSocketFrame instanceof CloseWebSocketFrame) {
            ServiceChannel serviceChannel = WSServiceChannelManager.get(ctx.channel());
            if (serviceChannel != null) {
                serviceChannel.getWebSocketServerHandshaker().close(ctx.channel(), (CloseWebSocketFrame) webSocketFrame.retain());
            }
            return;
        }

        // message
        // 处理文本消息、二进制消息
        WSServiceChannelManager.onMessage(ctx.channel(), webSocketFrame);
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        log.debug("channel读取完成");
        ctx.flush();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.debug("channel断开");
        WSServiceChannelManager.onClose(ctx.channel());
        WSServiceChannelManager.remove(ctx.channel(), true);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.debug("异常捕获");
        WSServiceChannelManager.onError(ctx.channel(), cause);
        WSServiceChannelManager.remove(ctx.channel(), true);
    }

}
