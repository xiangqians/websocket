package com.xiangqian.ws.server;

import com.xiangqian.ws.server.config.ServiceChannel;
import com.xiangqian.ws.util.IOUtils;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xiangqian
 * @date 13:58 2020/01/11
 */
@Slf4j
public class WSHttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("新连接加入");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        log.debug("读取channel信息");

        // Handle a bad request.
        if (!request.decoderResult().isSuccess()) {
            sendHttpResponse(ctx, request, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        // Allow only GET methods.
        if (!HttpMethod.GET.equals(request.method())
                || !"websocket".equals(request.headers().get("Upgrade"))
                || !HttpUtil.isKeepAlive(request)) {
            sendHttpResponse(ctx, request, HttpResponseStatus.FORBIDDEN);
            return;
        }

        // 拒绝不合法的请求，并返回错误信息
        ServiceChannel serviceChannel = WSServiceChannelManager.add(ctx.channel(), request);
        log.debug("serviceChannel=" + serviceChannel);
        if (serviceChannel == null) {
            sendHttpResponse(ctx, request, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        // 握手
        shakeHands(ctx, request, serviceChannel);
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

    // ////////////////////////////////

    private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest request, HttpResponseStatus status) {
        WSServiceChannelManager.remove(ctx.channel(), false);

        DefaultFullHttpResponse response = new DefaultFullHttpResponse(request.protocolVersion(), status);
        HttpUtil.setKeepAlive(response, false);

//        ByteBuf buffer = Unpooled.copiedBuffer(response.status().toString(), CharsetUtil.UTF_8);
//        response.content().writeBytes(buffer);
//        buffer.release();
        //
        ByteBufUtil.writeUtf8(response.content(), response.status().toString());

        HttpUtil.setContentLength(response, response.content().readableBytes());
        ctx.channel().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    // 握手
    private void shakeHands(ChannelHandlerContext ctx, FullHttpRequest request, ServiceChannel serviceChannel) {

        String webSocketURL = "ws://" + WSConfig.SERVER.getHost() + ":" + WSConfig.SERVER.getPort() + WSConfig.SERVER.getPath() + serviceChannel.getService().getPath(); // WebSocket地址
        String subprotocols = null; // 子协议
        boolean allowExtensions = false; // 是否允许扩展协议
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(webSocketURL, subprotocols, allowExtensions, IOUtils.MB);
        log.debug("webSocketURL=" + webSocketURL);

        // 获取WS握手对象
        WebSocketServerHandshaker webSocketServerHandshaker = wsFactory.newHandshaker(request);

        if (webSocketServerHandshaker == null) {
//            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            sendHttpResponse(ctx, request, HttpResponseStatus.UPGRADE_REQUIRED);
            return;
        }

        webSocketServerHandshaker.handshake(ctx.channel(), request);

        serviceChannel.setWebSocketServerHandshaker(webSocketServerHandshaker);

        // open
        WSServiceChannelManager.onOpen(ctx.channel());
    }

}
