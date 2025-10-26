package com.x.api.gateway.service.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 日志过滤器 - 记录请求和响应信息
 */
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * 请求ID头名称
     */
    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        // 生成请求ID
        String requestId = UUID.randomUUID().toString();

        // 获取请求信息
        ServerHttpRequest request = exchange.getRequest();
        URI uri = request.getURI();
        String method = request.getMethod().name();
        String path = uri.getPath();
        String clientIp = request.getRemoteAddress() != null ? request.getRemoteAddress().getAddress().getHostAddress() : "unknown";

        // 记录请求开始时间
        long startTime = System.currentTimeMillis();

        // 在请求头中添加请求ID
        ServerHttpRequest modifiedRequest = request.mutate()
                .header(REQUEST_ID_HEADER, requestId)
                .build();

        ServerWebExchange modifiedExchange = exchange.mutate().request(modifiedRequest).build();

        // 记录请求信息
        logger.info("[{}] Request received: {} {}, Client IP: {}, Time: {}",
                requestId, method, path, clientIp, LocalDateTime.now().format(formatter));

        // 记录请求头信息（可选，生产环境可能需要根据敏感程度决定是否记录）
        request.getHeaders().forEach((name, values) -> {
            // 避免记录敏感信息
            if (!name.toLowerCase().contains("authorization") && !name.toLowerCase().contains("token")) {
                logger.debug("[{}] Request Header: {}={}", requestId, name, values);
            }
        });

        // 执行过滤器链并记录响应信息
        return chain.filter(modifiedExchange).then(Mono.fromRunnable(() -> {
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            // 获取响应状态码
            int statusCode = modifiedExchange.getResponse().getStatusCode() != null
                    ? modifiedExchange.getResponse().getStatusCode().value()
                    : 0;

            // 记录响应信息
            logger.info("[{}] Request completed: {} {}, Status: {}, Execution Time: {}ms, Time: {}",
                    requestId, method, path, statusCode, executionTime, LocalDateTime.now().format(formatter));

            // 如果执行时间过长，记录警告
            if (executionTime > 1000) {
                logger.warn("[{}] Slow request detected: {} {}, Execution Time: {}ms",
                        requestId, method, path, executionTime);
            }
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE; // 最高优先级，确保最先执行
    }
}