package com.x.api.gateway.service.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 请求拦截器示例 - 展示如何通过实现GlobalFilter接口拦截所有请求
 * 这个示例拦截器会记录请求计数、请求头信息，并可以修改请求和响应
 */
@Component
public class RequestInterceptorExample implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(RequestInterceptorExample.class);
    
    // 请求计数器，用于统计总请求数
    private final AtomicLong requestCounter = new AtomicLong(0);
    
    /**
     * 实现GlobalFilter接口的核心方法，对所有请求进行拦截处理
     * 
     * @param exchange 包含请求和响应信息的交换对象
     * @param chain 过滤器链，用于继续执行后续过滤器
     * @return Mono<Void> 表示异步处理结果
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        // 增加请求计数
        long requestId = requestCounter.incrementAndGet();
        
        // 获取请求信息
        ServerHttpRequest request = exchange.getRequest();
        String method = request.getMethodValue();
        String path = request.getURI().getPath();
        String clientIp = request.getRemoteAddress() != null 
                ? request.getRemoteAddress().getAddress().getHostAddress() 
                : "unknown";
        
        logger.info("[Request #{}] 拦截到请求: {} {}, 客户端IP: {}", 
                requestId, method, path, clientIp);
        
        // 示例1: 读取并检查特定请求头
        String customHeader = request.getHeaders().getFirst("X-Custom-Header");
        if (customHeader != null) {
            logger.info("[Request #{}] 发现自定义请求头: X-Custom-Header={}", requestId, customHeader);
        }
        
        // 示例2: 修改请求 - 添加自定义请求头
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-Gateway-Processed", "true")
                .header("X-Request-Sequence-Id", String.valueOf(requestId))
                .build();
        
        // 创建修改后的交换对象
        ServerWebExchange modifiedExchange = exchange.mutate().request(modifiedRequest).build();
        
        // 示例3: 在响应返回给客户端前添加响应头
        modifiedExchange.getResponse().getHeaders().add("X-Gateway-Response-Time", 
                String.valueOf(System.currentTimeMillis()));
        
        // 继续执行过滤器链，并在完成后进行额外处理
        return chain.filter(modifiedExchange).then(Mono.fromRunnable(() -> {
            // 请求处理完成后的操作
            int statusCode = modifiedExchange.getResponse().getStatusCode() != null 
                    ? modifiedExchange.getResponse().getStatusCode().value() 
                    : 0;
            
            logger.info("[Request #{}] 请求处理完成: {} {}, 状态码: {}", 
                    requestId, method, path, statusCode);
            
            // 可以在这里根据请求路径或状态码添加特定的后处理逻辑
            if (statusCode >= 400) {
                logger.warn("[Request #{}] 请求处理异常: {} {}, 状态码: {}", 
                        requestId, method, path, statusCode);
            }
        }));
    }
    
    /**
     * 设置过滤器的执行顺序
     * 值越小，优先级越高
     * 
     * @return 过滤器的顺序值
     */
    @Override
    public int getOrder() {
        // 注意：顺序值需要根据实际需求设置
        // 此示例使用中等优先级
        return 0;
    }
    
    /**
     * 获取当前请求总数（仅用于示例）
     * 
     * @return 总请求数
     */
    public long getTotalRequests() {
        return requestCounter.get();
    }
}