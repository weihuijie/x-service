package com.x.data.sync.service.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.iotdb.session.pool.SessionPool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * IotDB 连接池配置
 *
 * @author whj
 */
@Slf4j
@Configuration
public class IoTDBSessionPoolConfig {

    @Value("${iotdb.nodeUrls:localhost:6667}")
    private List<String> nodeUrls;

    @Value("${iotdb.username:root}")
    private String username;

    @Value("${iotdb.password:root}")
    private String password;

    /**
     * 创建连接池实例
     * @return iotdbSessionPool
     */
    @Bean("iotdbSessionPool")
    public SessionPool iotdbSessionPool() {

        return new SessionPool.Builder()
                // 基础连接配置
                .nodeUrls(nodeUrls)
                .user(username)
                .password(password)
                // 连接池大小配置
                .maxSize(50)
                .fetchSize(10000)
                .build();
    }
}