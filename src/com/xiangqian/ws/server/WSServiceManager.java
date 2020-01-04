package com.xiangqian.ws.server;

import com.xiangqian.ws.scope.WSRequest;
import com.xiangqian.ws.scope.WSSession;
import com.xiangqian.ws.util.StringUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service管理器
 *
 * @author xiangqian
 * @date 19:43 2020/01/01
 */
@Slf4j
public class WSServiceManager {

    private static final Map<ChannelId, ServiceChannel> SERVICE_CHANNEL_MAP;

    static {
        SERVICE_CHANNEL_MAP = new ConcurrentHashMap<>();
    }

    static boolean add(Channel channel, String path) {
        log.debug("path=" + path);
        WSConfig.Service service = WSConfig.getService(path);
        log.debug("service=" + service);
        if (service != null) {
            try {
                ServiceChannel serviceChannel = new ServiceChannel(service);
                SERVICE_CHANNEL_MAP.put(channel.id(), serviceChannel);
                return true;
            } catch (Exception e) {
                log.error("", e);
            }
        }
        return false;
    }

    static void remove(Channel channel, boolean isClose) {
        SERVICE_CHANNEL_MAP.remove(channel.id());
        if (isClose && channel.isActive()) {
            channel.close();
        }
    }

    static boolean onInit(Channel channel, HttpRequest request) {
        ServiceChannel serviceChannel = SERVICE_CHANNEL_MAP.get(channel.id());
        Method initMethod = serviceChannel.service.onInit;
        if (initMethod == null) {
            return true;
        }
        try {
            Object result = initMethod.invoke(serviceChannel.serviceInstance, new WSRequest(request.uri()));
            return result == null ? true : Boolean.valueOf(StringUtils.trim(result));
        } catch (Exception e) {
            log.error("", e);
        }
        return false;
    }

    static void onOpen(Channel channel) {
        ServiceChannel serviceChannel = SERVICE_CHANNEL_MAP.get(channel.id());
        Method openMethod = serviceChannel.service.onOpen;
        if (openMethod != null) {
            try {
                openMethod.invoke(serviceChannel.serviceInstance, new WSSession(channel));
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }

    static void onMessage(Channel channel, WebSocketFrame webSocketFrame) {
        ServiceChannel serviceChannel = SERVICE_CHANNEL_MAP.get(channel.id());
        Method messageMethod = serviceChannel.service.onMessage;
        if (messageMethod != null) {
            try {
                messageMethod.invoke(serviceChannel.serviceInstance, webSocketFrame);
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }

    static void onClose(Channel channel) {
        ServiceChannel serviceChannel = SERVICE_CHANNEL_MAP.get(channel.id());
        Method closeMethod = serviceChannel.service.onClose;
        if (closeMethod != null) {
            try {
                closeMethod.invoke(serviceChannel.serviceInstance);
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }

    static void onError(Channel channel, Throwable throwable) {
        ServiceChannel serviceChannel = SERVICE_CHANNEL_MAP.get(channel.id());
        Method errorMethod = serviceChannel.service.onError;
        if (errorMethod != null) {
            try {
                errorMethod.invoke(serviceChannel.serviceInstance, throwable);
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }

    private static class ServiceChannel {
        WebSocketServerHandshaker webSocketServerHandshaker; // WS握手对象
        WSConfig.Service service;
        Object serviceInstance;

        public ServiceChannel(WSConfig.Service service) throws IllegalAccessException, InstantiationException {
            this.service = service;
            this.serviceInstance = this.service.clazz.newInstance();
        }
    }

}
