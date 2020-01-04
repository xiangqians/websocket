package com.xiangqian.ws.server;

import com.xiangqian.ws.scope.WSSession;
import com.xiangqian.ws.util.IOUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xiangqian
 * @date 13:59 2020/01/04
 */
@Slf4j
public class WSHandler extends SimpleChannelInboundHandler<Object> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("新连接加入");
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object obj) throws Exception {
        log.debug("读取channel信息");
        try {
            // HTTP协议接入
            if (obj instanceof FullHttpRequest) {
                handleFullHttpRequest(ctx, (FullHttpRequest) obj);
            }
            // WebSocket协议处理
            else if (obj instanceof WebSocketFrame) {
                handlerWebSocketFrame(ctx, (WebSocketFrame) obj);
            }
            // 无法解析请求方式
            else {
                log.error("无法解析请求方式，" + (obj == null ? null : obj.getClass()));
                badRequest(ctx);
            }
        } catch (Exception e) {
            log.error("channelRead异常", e);
            throw e;
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        log.debug("channel读取完成");
        ctx.flush();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.debug("channel断开");
        WSServiceManager.onClose(ctx.channel());
        WSServiceManager.remove(ctx.channel(), true);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.debug("异常捕获");
        WSServiceManager.onError(ctx.channel(), cause);
        WSServiceManager.remove(ctx.channel(), true);
    }

    // ////////////////////////////////

    private WebSocketServerHandshaker webSocketServerHandshaker;

    private void handleFullHttpRequest(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) {
        // 拒绝不合法的请求，并返回错误信息
        if (!fullHttpRequest.decoderResult().isSuccess()
                || (!"websocket".equals(fullHttpRequest.headers().get("Upgrade")))
                || !HttpUtil.isKeepAlive(fullHttpRequest)

                // add
                || WSServiceManager.add(ctx.channel(), fullHttpRequest.uri())

                // init
                || WSServiceManager.onInit(ctx.channel(), fullHttpRequest)) {
            StringBuilder builder = new StringBuilder();
            builder.append("不合法的请求: ");
            builder.append("uri").append("=").append(fullHttpRequest.uri()).append(", ");
            builder.append("Upgrade").append("=").append(fullHttpRequest.headers().get("Upgrade")).append(", ");
            builder.append("isKeepAlive").append("=").append(HttpUtil.isKeepAlive(fullHttpRequest)).append(", ");
            log.error(builder.toString());
            badRequest(ctx);
            return;
        }

        String webSocketURL = "ws://" + WSConfig.SERVER.host + ":" + WSConfig.SERVER.port + WSConfig.SERVER.path; // WebSocket地址
        String subprotocols = null; // 子协议
        boolean allowExtensions = false; // 是否允许扩展协议
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(webSocketURL, subprotocols, allowExtensions, IOUtils.MB);

        // 获取WS握手对象
        webSocketServerHandshaker = wsFactory.newHandshaker(fullHttpRequest);

        if (webSocketServerHandshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            webSocketServerHandshaker.handshake(ctx.channel(), fullHttpRequest);
        }

        // open
        WSServiceManager.onOpen(ctx.channel());
    }

    private void handlerWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame webSocketFrame) {
        // 判断是否关闭链路的指令
        if (webSocketFrame instanceof CloseWebSocketFrame) {
            webSocketServerHandshaker.close(ctx.channel(), (CloseWebSocketFrame) webSocketFrame.retain());
            return;
        }

        // 判断是否ping消息
        if (webSocketFrame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(webSocketFrame.content().retain()));
            return;
        }

        // message
        // 处理文本消息、二进制消息
        WSServiceManager.onMessage(ctx.channel(), webSocketFrame);
    }


    /**
     * 400, "Bad Request"
     *
     * @param ctx
     */
    private void badRequest(ChannelHandlerContext ctx) {
        WSServiceManager.remove(ctx.channel(), false);

        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
        ByteBuf buffer = Unpooled.copiedBuffer(response.status().toString(), CharsetUtil.UTF_8);
        response.content().writeBytes(buffer);
        buffer.release();
        HttpUtil.setContentLength(response, response.content().readableBytes());
        ctx.channel().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }


}
