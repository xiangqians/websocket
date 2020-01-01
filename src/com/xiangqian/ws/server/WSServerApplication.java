package com.xiangqian.ws.server;

import com.xiangqian.ws.annotation.WSServerConfiguration;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * WebSocket应用启动
 *
 * @author xiangqian
 * @date 15:39 2020/01/01
 */
@Slf4j
public class WSServerApplication {

    private WSServerApplication(Class<?> primarySource, String... args) throws IOException {
        WSConfig.init(primarySource.getDeclaredAnnotation(WSServerConfiguration.class), args);
    }

    private void run() throws InterruptedException {
        log.info("正在启动WebSocket服务器...");
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(2);
        NioEventLoopGroup workGroup = new NioEventLoopGroup(6);
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workGroup);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.childHandler(new WebSocketServerInitializer());
            bootstrap.option(ChannelOption.SO_BACKLOG, 128);
//            bootstrap.option(ChannelOption.SO_SNDBUF, 32 * 1024);
//            bootstrap.option(ChannelOption.SO_RCVBUF, 32 * 1024);
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture channelFuture = bootstrap.bind(WSConfig.SERVER.port);
            channelFuture.sync();

            log.info("WebSocket服务器启动成功!");
            log.info("[WebSocket path] http://localhost:" + WSConfig.SERVER.port + WSConfig.SERVER.path);

            channelFuture.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
            log.info("WebSocket服务器已关闭!");
        }
    }

    public static class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel sc) {
            ChannelPipeline channelPipeline = sc.pipeline();
            channelPipeline.addLast("logging", new LoggingHandler("DEBUG"));
            channelPipeline.addLast("http-codec", new HttpServerCodec());
            channelPipeline.addLast("http-chunked", new ChunkedWriteHandler());
            channelPipeline.addLast("aggregator", new HttpObjectAggregator(1024 * 64));
            channelPipeline.addLast("handler", new WSHandler());
        }
    }

    public static void run(Class<?> primarySource, String... args) {
        try {
            new WSServerApplication(primarySource, args).run();
        } catch (Exception e) {
            log.error("", e);
        }
    }


}
