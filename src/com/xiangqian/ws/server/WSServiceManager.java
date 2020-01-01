package com.xiangqian.ws.server;

import com.xiangqian.ws.scope.WSRequest;
import com.xiangqian.ws.util.StringUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.handler.codec.http.HttpRequest;
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

    static boolean add(Channel channel, String path) throws InstantiationException, IllegalAccessException {
        log.debug("path=" + path);
        WSConfig.Service service = WSConfig.getService(path);
        log.debug("service=" + service);
        if (service != null) {
            ServiceChannel serviceChannel = new ServiceChannel(service);
            SERVICE_CHANNEL_MAP.put(channel.id(), serviceChannel);
            return true;
        }
        return false;
    }


    static boolean init(Channel channel, HttpRequest request) throws InvocationTargetException, IllegalAccessException {
        ServiceChannel serviceChannel = SERVICE_CHANNEL_MAP.get(channel.id());
        Method initMethod = serviceChannel.service.onInit;
        if (initMethod == null) {
            return true;
        }
        Object result = initMethod.invoke(serviceChannel.serviceInstance, new WSRequest(request.uri()));
        return result == null ? true : Boolean.valueOf(StringUtils.trim(result));
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
