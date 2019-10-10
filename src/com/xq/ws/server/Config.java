package com.xq.ws.server;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xiangqian
 */
public class Config {

    // Application Config
    protected static final Application APPLICATION;

    /**
     * key:
     *
     * @see Server#path
     * <p>
     * value:
     * @see Server
     */
    protected static final Map<String, Server> SERVER_MAP;

    static {
        APPLICATION = new Application();
        SERVER_MAP = new HashMap<>();
    }

    /**
     * @param uri eg: /websocket/hello
     * @return
     */
    protected static Server getServer(String uri) {
        int index = -1;
        if((index=uri.indexOf("?")) > 0){
            uri = uri.substring(0, index);
        }
        String servicePath = uri.replace(APPLICATION.path, "");
        return SERVER_MAP.get(servicePath);
    }

    protected static class Server {
        protected String path;
        protected Class<?> clazz;
        protected Method onInit;
        protected Method onOpen;
        protected Method onClose;
        protected Method onMessage;
        protected Method onError;
    }

    protected static class Application {
        protected String host = "127.0.0.1";
        protected int port;
        protected String path;
        protected String[] basePackages;
    }
}
