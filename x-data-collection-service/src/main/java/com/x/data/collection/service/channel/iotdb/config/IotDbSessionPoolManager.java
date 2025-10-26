package com.x.data.collection.service.channel.iotdb.config;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.iotdb.session.pool.SessionPool;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
@Slf4j
@ConfigurationProperties(prefix = "spring.iotdb")
@Setter
public class IotDbSessionPoolManager {

    private String username;

    private String password;

    private String url;

    private String host;

    private int maxSize = 50; // 默认连接池大小

    private volatile SessionPool sessionPool;

    public SessionPool getSessionPool() {
        if (sessionPool == null) {
            synchronized (this) {
                if (sessionPool == null) {
                    sessionPool = new SessionPool(url, Integer.parseInt(host), username, password, maxSize);
                }
            }
        }
        return sessionPool;
    }

    @PostConstruct
    public void init()  {
        // 创建 SessionPool
        log.info("====SessionPool init====");
        if (sessionPool == null) {
            synchronized (this) {
                if (sessionPool == null) {
                    sessionPool = new SessionPool(url, Integer.parseInt(host), username, password, maxSize);
                    log.info("Initialized IoTDB SessionPool with max size: {}", maxSize);
                }
            }
        }
    }

    @PreDestroy
    public void destroy() {
        // 关闭 SessionPool
        log.info("====SessionPool destroy====");
        close();
    }

    /**
     * 关闭连接池
     */
    public void close() {
        if (sessionPool != null) {
            synchronized (this) {
                if (sessionPool != null) {
                    sessionPool.close();
                    sessionPool = null;
                }
            }
        }
    }

}