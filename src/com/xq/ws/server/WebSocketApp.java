package com.xq.ws.server;

import com.xq.ws.Session;
import com.xq.ws.annotation.*;
import com.xq.ws.exception.UndefinedException;
import com.xq.ws.http.WSHttpRequest;
import com.xq.ws.util.ClassUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author xiangqian
 */
@Slf4j
public class WebSocketApp {

    private Class<?> primarySource;

    private WebSocketApp(Class<?> primarySource) {
        this.primarySource = primarySource;
    }

    private void initApplication() throws Exception {
        WebSocketApplication webSocketApplication = primarySource.getDeclaredAnnotation(WebSocketApplication.class);
        if (webSocketApplication == null) {
            throw new UndefinedException("@WebSocketApplication annotation not found");
        }
        Config.APPLICATION.port = webSocketApplication.port();
        Config.APPLICATION.path = webSocketApplication.path();
        Config.APPLICATION.basePackages = webSocketApplication.basePackages();
        if (Config.APPLICATION.basePackages == null) {
            throw new UndefinedException("basePackages unspecified");
        }
    }


    private void initServer() throws Exception {
        List<String> classNames = null;
        for (String packageName : Config.APPLICATION.basePackages) {
            Class<?> clazz = null;
            WebSocketServer webSocketServer = null;
            Config.Server server = null;

            // 获取指定包下的所有全类名
            classNames = ClassUtils.getClassNames(packageName);

            for (String className : classNames) {
                try {
                    clazz = Class.forName(className);
                    webSocketServer = clazz.getDeclaredAnnotation(WebSocketServer.class);
                    if (webSocketServer == null) {
                        continue;
                    }

                    server = new Config.Server();
                    server.path = webSocketServer.path();
                    server.clazz = clazz;

                    Method[] methods = clazz.getDeclaredMethods();
                    Class<?>[] parameterTypes = null;
                    for (Method method : methods) {
                        // on init
                        if (method.getDeclaredAnnotation(OnInit.class) != null) {
                            if ((parameterTypes = method.getParameterTypes()).length == 1
                                    && (parameterTypes[0] == FullHttpRequest.class || parameterTypes[0] == WSHttpRequest.class)) {
                                method.setAccessible(true);
                                server.onInit = method;
                            }
                        }
                        // on open
                        else if (method.getDeclaredAnnotation(OnOpen.class) != null) {
                            if ((parameterTypes = method.getParameterTypes()).length == 1 && parameterTypes[0] == Session.class) {
                                method.setAccessible(true);
                                server.onOpen = method;
                            }
                        }
                        // on message
                        else if (method.getDeclaredAnnotation(OnMessage.class) != null) {
                            if ((parameterTypes = method.getParameterTypes()).length == 1 && parameterTypes[0] == WebSocketFrame.class) {
                                method.setAccessible(true);
                                server.onMessage = method;
                            }

                        }
                        // on close
                        else if (method.getDeclaredAnnotation(OnClose.class) != null) {
                            if ((parameterTypes = method.getParameterTypes()).length == 0) {
                                method.setAccessible(true);
                                server.onClose = method;
                            }

                        }
                        // on error
                        else if (method.getDeclaredAnnotation(OnError.class) != null) {
                            if ((parameterTypes = method.getParameterTypes()).length == 1 && parameterTypes[0] == Throwable.class) {
                                method.setAccessible(true);
                                server.onError = method;
                            }
                        }
                    }

                    // put
                    Config.SERVER_MAP.put(server.path, server);

                } catch (ClassNotFoundException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    private void init() throws Exception {
        initApplication();
        initServer();
    }

    private void start() throws Exception {
        log.info("正在启动WebSocket服务器...");
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workGroup);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.childHandler(new WebSocketServerInitializer());
            bootstrap.option(ChannelOption.SO_BACKLOG, 128);
//            bootstrap.option(ChannelOption.SO_SNDBUF, 32 * 1024);
//            bootstrap.option(ChannelOption.SO_RCVBUF, 32 * 1024);
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture channelFuture = bootstrap.bind(Config.APPLICATION.port);
            channelFuture.sync();

            log.info("WebSocket服务器启动成功");
            log.info("[WebSocket path] http://localhost:" + Config.APPLICATION.port + Config.APPLICATION.path);

            channelFuture.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
            log.info("WebSocket服务器已关闭");
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
            channelPipeline.addLast("handler", new WebSocketHandler());
        }
    }

    private void run(String... args) throws Exception {
        init();
        start();
    }

    public static void run(Class<?> primarySource, String... args) throws Exception {
        new WebSocketApp(primarySource).run(args);
    }

}
