package com.x.data.collection.service.adapter.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * 高性能适配器配置类
 * 配置高性能线程池和Web服务器
 *
 * @author whj
 */
@Configuration
public class HighPerformanceAdapterConfig {

    /**
     * 配置高性能线程池用于处理设备数据
     * @return ExecutorService
     */
    @Bean("deviceDataExecutor")
    public ExecutorService deviceDataExecutor() {
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("device-data-processor-%d")
                .setDaemon(false)
                .build();
        
        // 核心线程数：CPU核心数的2倍
        int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;
        // 最大线程数：CPU核心数的4倍
        int maximumPoolSize = Runtime.getRuntime().availableProcessors() * 4;
        // 线程空闲时间（秒）
        long keepAliveTime = 60L;
        
        // 使用自定义的ThreadPoolExecutor以获得更好的控制
        // 设置合理的队列大小
        // 当线程池满时，由调用线程处理任务

        return new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10000), // 设置合理的队列大小
                threadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy() // 当线程池满时，由调用线程处理任务
        );
    }
}