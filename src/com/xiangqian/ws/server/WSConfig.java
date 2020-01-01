package com.xiangqian.ws.server;

import com.xiangqian.ws.annotation.*;
import com.xiangqian.ws.exception.UndefinedException;
import com.xiangqian.ws.scope.WSRequest;
import com.xiangqian.ws.scope.WSSession;
import com.xiangqian.ws.util.ClassUtils;
import com.xiangqian.ws.util.NumberUtils;
import com.xiangqian.ws.util.StringUtils;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xiangqian
 * @date 16:39 2020/01/01
 */
@Slf4j
public class WSConfig {

    // Server
    static final Server SERVER;

    private static final String ARG_WS_SERVER_PORT = "--ws.server.port=";
    private static final String ARG_WS_SERVER_PATH = "--ws.server.path=";

    /**
     * key: {@link Service#path}
     * <p>
     * value: {@link Service}
     */
    static final Map<String, Service> SERVICE_MAP;

    static {
        SERVER = new Server();
        SERVICE_MAP = new HashMap<>();
    }

    static Service getService(String path) {
        for (Map.Entry<String, Service> entry : SERVICE_MAP.entrySet()) {
            if (entry.getKey().equals(path)) {
                return entry.getValue();
            }
        }
        return null;
    }


    /**
     * @param wsServerConfiguration
     * @param args
     * @throws IOException
     */
    static void init(WSServerConfiguration wsServerConfiguration, String... args) throws IOException {
        initServer(wsServerConfiguration, args);
        initService();

        log.debug("args: " + Arrays.asList(args));
        log.debug("SERVER: " + SERVER);
        for (Map.Entry<String, Service> entry : SERVICE_MAP.entrySet()) {
            log.debug("SERVICE_MAP -> " + entry.getKey() + " : " + entry.getValue());
        }
    }

    private static void initServer(WSServerConfiguration wsServerConfiguration, String... args) {
        if (wsServerConfiguration == null) {
            throw new UndefinedException("@WSServerConfiguration annotation not found!");
        }

        SERVER.host = "127.0.0.1";

        if (args != null) {
            for (String arg : args) {
                if (arg.startsWith(ARG_WS_SERVER_PORT)) {
                    SERVER.port = NumberUtils.convert2int(arg.replace(ARG_WS_SERVER_PORT, ""), 0);
                } else if (arg.startsWith(ARG_WS_SERVER_PATH)) {
                    String path = StringUtils.trim(arg.replace(ARG_WS_SERVER_PATH, ""));
                    if (!"".equals(path)) {
                        SERVER.path = path;
                    }
                }
            }
        }

        if (SERVER.port == 0) {
            SERVER.port = wsServerConfiguration.port();
        }
        if (SERVER.path == null) {
            SERVER.path = wsServerConfiguration.path();
        }

        if (wsServerConfiguration.basePackages() == null) {
            throw new UndefinedException("@WSServerConfiguration param basePackages unspecified!");
        }
        SERVER.basePackages = wsServerConfiguration.basePackages();
    }

    private static void initService() throws IOException {
        List<String> classNames = null;
        for (String packageName : SERVER.basePackages) {
            Class<?> serviceClass = null;
            WSService wsServiceAnnotation = null;
            Service service = null;

            // 获取指定包下的所有全类名
            classNames = ClassUtils.getClassNames(packageName);
            for (String className : classNames) {
                try {
                    serviceClass = Class.forName(className);
                    wsServiceAnnotation = serviceClass.getDeclaredAnnotation(WSService.class);
                    if (wsServiceAnnotation == null) {
                        continue;
                    }

                    service = new Service();
                    service.path = wsServiceAnnotation.path();
                    service.clazz = serviceClass;

                    Method[] methods = serviceClass.getDeclaredMethods();
                    Class<?>[] parameterTypes = null;
                    for (Method method : methods) {
                        // on init
                        if (method.getDeclaredAnnotation(OnInit.class) != null) {
                            if ((parameterTypes = method.getParameterTypes()).length == 1 && parameterTypes[0] == WSRequest.class) {
                                method.setAccessible(true);
                                service.onInit = method;
                            }
                        }
                        // on open
                        else if (method.getDeclaredAnnotation(OnOpen.class) != null) {
                            if ((parameterTypes = method.getParameterTypes()).length == 1 && parameterTypes[0] == WSSession.class) {
                                method.setAccessible(true);
                                service.onOpen = method;
                            }
                        }
                        // on message
                        else if (method.getDeclaredAnnotation(OnMessage.class) != null) {
                            if ((parameterTypes = method.getParameterTypes()).length == 1 && parameterTypes[0] == WebSocketFrame.class) {
                                method.setAccessible(true);
                                service.onMessage = method;
                            }
                        }
                        // on close
                        else if (method.getDeclaredAnnotation(OnClose.class) != null) {
                            if ((parameterTypes = method.getParameterTypes()).length == 0) {
                                method.setAccessible(true);
                                service.onClose = method;
                            }

                        }
                        // on error
                        else if (method.getDeclaredAnnotation(OnError.class) != null) {
                            if ((parameterTypes = method.getParameterTypes()).length == 1 && parameterTypes[0] == Throwable.class) {
                                method.setAccessible(true);
                                service.onError = method;
                            }
                        }
                    }

                    // put
                    SERVICE_MAP.put(service.path, service);

                } catch (Exception e) {
                    log.error("", e);
                }
            }
        }
    }

    /**
     * @param uri eg: /websocket/hello
     * @return
     */
    protected static Service initServer(String uri) {
        int index = -1;
        if ((index = uri.indexOf("?")) > 0) {
            uri = uri.substring(0, index);
        }
        String servicePath = uri.replace(SERVER.path, "");
        return SERVICE_MAP.get(servicePath);
    }

    /**
     * 应用配置信息
     */
    @ToString
    static class Server {
        String host; // WSApplication host
        int port; // WebSocket监听端口
        String path; // WebSocket请求根路径
        String[] basePackages; // 扫描Server所在的包
    }

    @ToString
    static class Service {

        /**
         * server服务路径{@link com.xiangqian.ws.annotation.WSService}
         */
        String path;

        /**
         * service class
         */
        Class<?> clazz;

        /**
         * server {@link com.xiangqian.ws.annotation.OnInit}
         */
        Method onInit;

        /**
         * server {@link com.xiangqian.ws.annotation.OnOpen}
         */
        Method onOpen;

        /**
         * server {@link com.xiangqian.ws.annotation.OnMessage}
         */
        Method onMessage;

        /**
         * server {@link com.xiangqian.ws.annotation.OnClose}
         */
        Method onClose;

        /**
         * server {@link com.xiangqian.ws.annotation.OnError}
         */
        Method onError;
    }

}
