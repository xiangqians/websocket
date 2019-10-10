package com.xq.ws.server;

import com.xq.ws.Session;
import com.xq.ws.http.WSHttpRequest;
import io.netty.channel.ChannelId;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xiangqian
 */
@Slf4j
public class Service {

    private static final Map<ChannelId, Value> SERVICE_MAP = new ConcurrentHashMap<>();

    /**
     * 添加Service
     *
     * @param channelId
     * @param server
     * @return
     * @throws Exception
     */
    protected static boolean add(ChannelId channelId, Config.Server server) throws Exception {
        SERVICE_MAP.put(channelId, new Value(server));
        return true;
    }

    private static Value get(ChannelId channelId) {
        return SERVICE_MAP.get(channelId);
    }

    protected static Value remove(ChannelId channelId) {
        return SERVICE_MAP.remove(channelId);
    }

    public static int size() {
        return SERVICE_MAP.size();
    }

    protected static class Value {
        protected Config.Server server;
        protected Object serverInstance;

        protected Value(Config.Server server) throws Exception {
            this.server = server;
            this.serverInstance = server.clazz.newInstance();
        }
    }

    /**
     * Server操作动作
     */
    private static enum Operation {
        INIT, OPEN, MESSAGE, CLOSE, ERROR;
    }

    protected static class Invoke {
        protected static Object init(ChannelId channelId, HttpRequest request) throws Exception {
            return invoke(Operation.INIT, channelId, request);
        }

        protected static Object open(ChannelId channelId, Session session) throws Exception {
            return invoke(Operation.OPEN, channelId, session);
        }

        protected static Object message(ChannelId channelId, WebSocketFrame webSocketFrame) throws Exception {
            return invoke(Operation.MESSAGE, channelId, webSocketFrame);
        }

        protected static Object close(ChannelId channelId) throws Exception {
            return invoke(Operation.CLOSE, channelId, null);
        }

        protected static Object error(ChannelId channelId, Throwable throwable) throws Exception {
            return invoke(Operation.ERROR, channelId, throwable);
        }

        private static Object invoke(Operation operation, ChannelId channelId, Object arg) throws Exception {
            Value value = Service.get(channelId);
            if (value == null) {
                return null;
            }

            Object result = null;
            if (operation == Operation.INIT) {
                if (value.server.onInit != null) {
                    result = value.server.onInit.invoke(value.serverInstance, postProcessingHttpRequest(value.server.onInit, (HttpRequest) arg));
                }
            } else if (operation == Operation.OPEN) {
                if (value.server.onOpen != null) {
                    result = value.server.onOpen.invoke(value.serverInstance, arg);
                }
            } else if (operation == Operation.MESSAGE) {
                if (value.server.onMessage != null) {
                    result = value.server.onMessage.invoke(value.serverInstance, arg);
                }
            } else if (operation == Operation.CLOSE) {
                if (value.server.onClose != null) {
                    result = value.server.onClose.invoke(value.serverInstance);
                }
            } else if (operation == Operation.ERROR) {
                if (value.server.onError != null) {
                    result = value.server.onError.invoke(value.serverInstance, arg);
                }
            }
            return result;
        }
    }

    private static HttpRequest postProcessingHttpRequest(Method init, HttpRequest request) {
        Class<?> parameterType = init.getParameterTypes()[0];
        if (parameterType == WSHttpRequest.class) {
            WSHttpRequest wsHttpRequest = new WSHttpRequest();
            String uri = request.uri();
            int index = -1;
            if ((index = uri.indexOf("?")) <= 0) {
                return wsHttpRequest;
            }

            Map<String, String> parameterMap = new HashMap<>();
            String[] parameterKVArr = uri.substring(index + 1).split("&");
            String[] parameterKV = null;
            for (String parameterKVStr : parameterKVArr) {
                parameterKV = parameterKVStr.split("=");
                parameterMap.put(parameterKV[0], parameterKV.length > 1 ? parameterKV[1] : null);
            }
            try {
                Method method = WSHttpRequest.class.getDeclaredMethod("setParameter", Map.class);
                method.setAccessible(true);
                method.invoke(wsHttpRequest, parameterMap);
            } catch (Exception e) {
                log.error("执行WSHttpRequest.setParameter方法异常", e);
            }
            return wsHttpRequest;
        }
        return request;
    }

}
