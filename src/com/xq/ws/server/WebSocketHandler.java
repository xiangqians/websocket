package com.xq.ws.server;

import com.xq.ws.Session;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xiangqian
 */
@Slf4j
public class WebSocketHandler extends SimpleChannelInboundHandler<Object> {

    private WebSocketServerHandshaker webSocketServerHandshaker;

    /**
     * 加入新连接
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("加入新连接");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object obj) throws Exception {
        log.debug("通道信息读取");
        try {
            // HTTP协议接入
            if (obj instanceof FullHttpRequest) {
                handleHttpRequest(channelHandlerContext, (FullHttpRequest) obj);
            }
            // WebSocket协议处理
            else if (obj instanceof WebSocketFrame) {
                handlerWebSocketFrame(channelHandlerContext, (WebSocketFrame) obj);
            }
            // 无法解析请求方式
            else {
                System.out.println("===============无法解析请求方式============");
                String error = "无法解析请求方式, " + (obj == null ? null : obj.getClass());
                log.error(error);
                badRequest(channelHandlerContext);
            }
        } catch (Exception e) {
            log.error("channelRead异常", e);
            throw e;
        }
    }

    /**
     * channel读取完成
     *
     * @param channelHandlerContext
     * @throws Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext channelHandlerContext) throws Exception {
        channelHandlerContext.flush();
    }

    /**
     * 断开连接
     *
     * @param channelHandlerContext
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext channelHandlerContext) throws Exception {
        log.debug("断开连接");
        ChannelId channelId = channelHandlerContext.channel().id();
        try {
            Service.Invoke.close(channelId);
        } catch (Exception e) {
            log.error("执行close方法异常", e);
        } finally {
            Service.remove(channelId);
            this.closeChannel(channelHandlerContext.channel());
        }
    }

    /**
     * 异常捕获
     *
     * @param channelHandlerContext
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable cause) throws Exception {
        log.debug("异常捕获");
        ChannelId channelId = channelHandlerContext.channel().id();
        try {
            Service.Invoke.error(channelId, cause);
        } catch (Exception e) {
            log.error("执行error方法异常", e);
        } finally {
            Service.remove(channelId);
            this.closeChannel(channelHandlerContext.channel());
        }
    }

    /**
     * 400, "Bad Request"
     *
     * @param channelHandlerContext
     */
    private void badRequest(ChannelHandlerContext channelHandlerContext) {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
        ByteBuf buf = Unpooled.copiedBuffer(response.status().toString(), CharsetUtil.UTF_8);
        response.content().writeBytes(buf);
        buf.release();
        HttpUtil.setContentLength(response, response.content().readableBytes());
        channelHandlerContext.channel().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private void handleHttpRequest(ChannelHandlerContext channelHandlerContext, FullHttpRequest request) throws Exception {
        Config.Server server = null;
        ChannelId channelId = channelHandlerContext.channel().id();
        Object init = null;

        // 拒绝不合法的请求，并返回错误信息
        if (!request.decoderResult().isSuccess()
                || (!"websocket".equals(request.headers().get("Upgrade")))
                || !HttpUtil.isKeepAlive(request)
                || (server = Config.getServer(request.uri())) == null
                || (Service.add(channelId, server) && Boolean.FALSE.equals((init = Service.Invoke.init(channelId, request))))) {
            StringBuilder builder = new StringBuilder();
            builder.append("不合法的请求: ");
            builder.append("uri").append("=").append(request.uri()).append(", ");
            builder.append("Upgrade").append("=").append(request.headers().get("Upgrade")).append(", ");
            builder.append("isKeepAlive").append("=").append(HttpUtil.isKeepAlive(request)).append(", ");
            builder.append("server").append("=").append(server);
            builder.append("init").append("=").append(init);
            log.error(builder.toString());
            badRequest(channelHandlerContext);
            return;
        }

        String webSocketURL = "ws://" + Config.APPLICATION.host + ":" + Config.APPLICATION.port + Config.APPLICATION.path; // WebSocket地址
        String subprotocols = null; // 子协议
        boolean allowExtensions = false; // 是否允许扩展协议
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(webSocketURL, subprotocols, allowExtensions);

        // 获取WS握手对象
        webSocketServerHandshaker = wsFactory.newHandshaker(request);

        if (webSocketServerHandshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(channelHandlerContext.channel());
        } else {
            webSocketServerHandshaker.handshake(channelHandlerContext.channel(), request);
        }

        // open
        Service.Invoke.open(channelId, new Session(channelHandlerContext.channel()));
    }

    private void handlerWebSocketFrame(ChannelHandlerContext channelHandlerContext, WebSocketFrame webSocketFrame) throws Exception {
        // 判断是否关闭链路的指令
        if (webSocketFrame instanceof CloseWebSocketFrame) {
            webSocketServerHandshaker.close(channelHandlerContext.channel(), (CloseWebSocketFrame) webSocketFrame.retain());
            return;
        }

        // 判断是否ping消息
        if (webSocketFrame instanceof PingWebSocketFrame) {
            channelHandlerContext.channel().write(new PongWebSocketFrame(webSocketFrame.content().retain()));
            return;
        }

        // 目前仅处理文本消息、二进制消息
        if (webSocketFrame instanceof TextWebSocketFrame || webSocketFrame instanceof BinaryWebSocketFrame) {
            Service.Invoke.message(channelHandlerContext.channel().id(), webSocketFrame);
        }
    }

    private void closeChannel(Channel channel) {
        if (channel != null && channel.isActive()) {
            channel.close();
        }
    }
}