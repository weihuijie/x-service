package com.x.data.collection.mqtt.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 业务线程池配置（与 MQTT IO 线程池隔离）
 */
@Configuration
public class BusinessThreadPoolConfig {

    /**
     * 核心业务线程池（处理 MQTT 异步业务）
     */
    @Bean(name = "mqttBusinessExecutor")
    public Executor mqttBusinessExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int corePoolSize = Runtime.getRuntime().availableProcessors() * 4; // 核心线程数：CPU核心数 * 2
        int maxPoolSize = Runtime.getRuntime().availableProcessors() * 8;  // 最大线程数：CPU核心数 * 4
        int queueCapacity = 20000; // 任务队列大小（根据业务调整，避免OOM）
        long keepAliveSeconds = 60; // 空闲线程存活时间

        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds((int) keepAliveSeconds);
        executor.setThreadNamePrefix("mqtt-business-"); // 线程名前缀（方便日志排查）

        // CallerRunsPolicy：让调用线程（MQTT IO线程）临时处理任务，避免任务丢失（牺牲部分IO性能，换可用性）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 初始化线程池
        executor.initialize();
        return executor;
    }
}