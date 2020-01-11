package com.xiangqian.ws.server;

import com.xiangqian.ws.scope.WSRequest;
import com.xiangqian.ws.scope.WSSession;
import com.xiangqian.ws.server.config.Service;
import com.xiangqian.ws.server.config.ServiceChannel;
import com.xiangqian.ws.util.StringUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Service管理器
 *
 * @author xiangqian
 * @date 19:43 2020/01/01
 */
@Slf4j
public class WSServiceChannelManager {

    private static final Map<ChannelId, ServiceChannel> SERVICE_CHANNEL_MAP;

    static {
        SERVICE_CHANNEL_MAP = new ConcurrentHashMap<>();
//        monitor();
    }

    private static void monitor() {
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    log.debug("SERVICE_CHANNEL_MAP存活数量：" + SERVICE_CHANNEL_MAP.size());
                }
            }
        }.start();
    }

    static ServiceChannel add(Channel channel, HttpRequest request) throws InstantiationException, IllegalAccessException {
        String path = request.uri().replace(WSConfig.SERVER.getPath(), "");
        int index = path.indexOf("?");
        if (index > 0) {
            path = path.substring(0, index);
        }
//        log.debug("path=" + path);
        Service service = WSConfig.SERVICE_MAP.get(path);
        if (service == null) {
            return null;
        }

        ServiceChannel serviceChannel = new ServiceChannel(service);
        if (!onInit(serviceChannel, request)) {
            return null;
        }

        SERVICE_CHANNEL_MAP.put(channel.id(), serviceChannel);
        return serviceChannel;
    }

    static ServiceChannel get(Channel channel) {
        return SERVICE_CHANNEL_MAP.get(channel.id());
    }

    static void remove(Channel channel, boolean isClose) {
        SERVICE_CHANNEL_MAP.remove(channel.id());
        if (isClose && channel.isActive()) {
            channel.close();
        }
    }

    static boolean onInit(ServiceChannel serviceChannel, HttpRequest request) {
        if (serviceChannel == null) {
            return false;
        }
        Method initMethod = serviceChannel.getService().getOnInit();
        if (initMethod == null) {
            return true;
        }
        try {
            Object result = initMethod.invoke(serviceChannel.getServiceInstance(), new WSRequest(request.uri()));
            return result == null ? true : Boolean.valueOf(StringUtils.trim(result));
        } catch (Exception e) {
            log.error("", e);
        }
        return false;
    }


    static void onOpen(Channel channel) {
        ServiceChannel serviceChannel = SERVICE_CHANNEL_MAP.get(channel.id());
        if (serviceChannel == null) {
            return;
        }
        Method openMethod = serviceChannel.getService().getOnOpen();
        if (openMethod != null) {
            try {
                openMethod.invoke(serviceChannel.getServiceInstance(), new WSSession(channel));
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }

    static void onMessage(Channel channel, WebSocketFrame webSocketFrame) {
        ServiceChannel serviceChannel = SERVICE_CHANNEL_MAP.get(channel.id());
        if (serviceChannel == null) {
            return;
        }
        Method messageMethod = serviceChannel.getService().getOnMessage();
        if (messageMethod != null) {
            try {
                messageMethod.invoke(serviceChannel.getServiceInstance(), webSocketFrame);
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }

    static void onClose(Channel channel) {
        ServiceChannel serviceChannel = SERVICE_CHANNEL_MAP.get(channel.id());
        if (serviceChannel == null) {
            return;
        }
        Method closeMethod = serviceChannel.getService().getOnClose();
        if (closeMethod != null) {
            try {
                closeMethod.invoke(serviceChannel.getServiceInstance());
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }

    static void onError(Channel channel, Throwable throwable) {
        ServiceChannel serviceChannel = SERVICE_CHANNEL_MAP.get(channel.id());
        if (serviceChannel == null) {
            return;
        }
        Method errorMethod = serviceChannel.getService().getOnError();
        if (errorMethod != null) {
            try {
                errorMethod.invoke(serviceChannel.getServiceInstance(), throwable);
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }


}
