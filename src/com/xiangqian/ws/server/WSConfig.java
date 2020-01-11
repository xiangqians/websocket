package com.xiangqian.ws.server;

import com.xiangqian.ws.annotation.*;
import com.xiangqian.ws.exception.UndefinedException;
import com.xiangqian.ws.scope.WSRequest;
import com.xiangqian.ws.scope.WSSession;
import com.xiangqian.ws.server.config.Server;
import com.xiangqian.ws.server.config.Service;
import com.xiangqian.ws.util.ClassUtils;
import com.xiangqian.ws.util.NumberUtils;
import com.xiangqian.ws.util.StringUtils;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
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
        initServer(args);
        initServer(wsServerConfiguration);
        initService();

        log.debug("args: " + Arrays.asList(args));
        log.debug("SERVER: " + SERVER);
        for (Map.Entry<String, Service> entry : SERVICE_MAP.entrySet()) {
            log.debug("SERVICE_MAP -> " + entry.getKey() + " : " + entry.getValue());
        }
    }

    private static void initServer(WSServerConfiguration wsServerConfiguration) {
        if (wsServerConfiguration == null) {
            throw new UndefinedException("@WSServerConfiguration annotation not found!");
        }

        if (SERVER.getPort() == 0) {
            SERVER.setPort(wsServerConfiguration.port());
        }

        if (SERVER.getPath() == null) {
            SERVER.setPath(wsServerConfiguration.path());
        }

        if (wsServerConfiguration.basePackages() == null) {
            throw new UndefinedException("@WSServerConfiguration param basePackages unspecified!");
        }
        SERVER.setBasePackages(wsServerConfiguration.basePackages());

    }

    private static void initServer(String... args) {
        SERVER.setHost("127.0.0.1");

        if (args != null) {
            for (String arg : args) {
                if (arg.startsWith(ARG_WS_SERVER_PORT)) {
                    int port = NumberUtils.convert2int(arg.replace(ARG_WS_SERVER_PORT, ""), 0);
                    if (port != 0) {
                        SERVER.setPort(port);
                    }
                } else if (arg.startsWith(ARG_WS_SERVER_PATH)) {
                    String path = StringUtils.trim(arg.replace(ARG_WS_SERVER_PATH, ""));
                    if ("".equals(path)) {
                        SERVER.setPath(path);
                    }
                }
            }
        }
    }

    private static void initService() throws IOException {
        List<String> classNames = null;
        for (String packageName : SERVER.getBasePackages()) {
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
                    service.setPath(wsServiceAnnotation.path());
                    service.setClazz(serviceClass);

                    Method[] methods = serviceClass.getDeclaredMethods();
                    Class<?>[] parameterTypes = null;
                    for (Method method : methods) {
                        // on init
                        if (method.getDeclaredAnnotation(OnInit.class) != null) {
                            if ((parameterTypes = method.getParameterTypes()).length == 1 && parameterTypes[0] == WSRequest.class) {
                                method.setAccessible(true);
                                service.setOnInit(method);
                            }
                        }
                        // on open
                        else if (method.getDeclaredAnnotation(OnOpen.class) != null) {
                            if ((parameterTypes = method.getParameterTypes()).length == 1 && parameterTypes[0] == WSSession.class) {
                                method.setAccessible(true);
                                service.setOnOpen(method);
                            }
                        }
                        // on message
                        else if (method.getDeclaredAnnotation(OnMessage.class) != null) {
                            if ((parameterTypes = method.getParameterTypes()).length == 1 && parameterTypes[0] == WebSocketFrame.class) {
                                method.setAccessible(true);
                                service.setOnMessage(method);
                            }
                        }
                        // on close
                        else if (method.getDeclaredAnnotation(OnClose.class) != null) {
                            if ((parameterTypes = method.getParameterTypes()).length == 0) {
                                method.setAccessible(true);
                                service.setOnClose(method);
                            }

                        }
                        // on error
                        else if (method.getDeclaredAnnotation(OnError.class) != null) {
                            if ((parameterTypes = method.getParameterTypes()).length == 1 && parameterTypes[0] == Throwable.class) {
                                method.setAccessible(true);
                                service.setOnError(method);
                            }
                        }
                    }

                    // put
                    SERVICE_MAP.put(service.getPath(), service);

                } catch (Exception e) {
                    log.error("", e);
                }
            }
        }
    }

//    /**
//     * @param uri eg: /websocket/hello
//     * @return
//     */
//    protected static Service initServer(String uri) {
//        int index = -1;
//        if ((index = uri.indexOf("?")) > 0) {
//            uri = uri.substring(0, index);
//        }
//        String servicePath = uri.replace(SERVER.path, "");
//        return SERVICE_MAP.get(servicePath);
//    }


}
