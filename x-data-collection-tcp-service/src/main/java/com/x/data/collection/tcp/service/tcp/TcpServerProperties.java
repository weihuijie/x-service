package com.x.data.collection.tcp.service.tcp;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * TCP 服务配置类
 */
@Component
@ConfigurationProperties(prefix = "tcp.server")
@Getter
@Setter
public class TcpServerProperties {
    // 默认配置（可在 application.yml 中覆盖）
    private String host = "0.0.0.0";
    private int port = 9101;
    private int bossThreads = 1;
    private int workerThreads = Runtime.getRuntime().availableProcessors() * 2;
    private int backlog = 10240;
    private int readIdleSeconds = 300;
    private int bufferSize = 16 * 1024;
    private int frameMaxLength = 64 * 1024;
}