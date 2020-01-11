package com.xiangqian.ws.server.config;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 应用配置信息
 *
 * @author xiangqian
 * @date 13:57 2020/01/11
 */
@NoArgsConstructor
@Data
public class Server {

    private String host; // WSApplication host
    private int port; // WebSocket监听端口
    private String path; // WebSocket请求根路径
    private String[] basePackages; // 扫描Server所在的包

}
