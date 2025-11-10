package com.x.api.gateway.service.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 参数校验过滤器 - 统一校验请求参数，防止恶意请求
 *
 * @author whj
 */
@Component
public class ParameterValidationFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(ParameterValidationFilter.class);

    // 敏感字符列表，用于检测潜在的恶意输入
    private static final List<String> SENSITIVE_PATTERNS = List.of(
        "<script", "javascript:", "vbscript:", "onload", "onerror",
        "alert(", "eval(", "expression(", "data:"
    );

    // 需要跳过校验的路径
    private static final List<String> SKIP_PATHS = List.of(
        "/health", "/actuator", "/swagger-ui", "/v3/api-docs"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 检查是否需要跳过校验
        if (shouldSkipValidation(path)) {
            return chain.filter(exchange);
        }

        // 校验查询参数
        if (hasSensitiveContent(request.getQueryParams())) {
            logger.warn("Potential malicious query parameters detected in request: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
            String errorMessage = "{\"code\":400,\"message\":\"Invalid request parameters\"}";
            byte[] bytes = errorMessage.getBytes(StandardCharsets.UTF_8);
            return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(bytes))
            );
        }

        // 校验路径参数
        if (hasSensitiveContentInPath(path)) {
            logger.warn("Potential malicious content detected in request path: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
            String errorMessage = "{\"code\":400,\"message\":\"Invalid request path\"}";
            byte[] bytes = errorMessage.getBytes(StandardCharsets.UTF_8);
            return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(bytes))
            );
        }

        return chain.filter(exchange);
    }

    /**
     * 检查是否应该跳过参数校验
     */
    private boolean shouldSkipValidation(String path) {
        return SKIP_PATHS.stream().anyMatch(path::startsWith);
    }

    /**
     * 检查参数中是否包含敏感内容
     */
    private boolean hasSensitiveContent(Map<String, List<String>> params) {
        for (Map.Entry<String, List<String>> entry : params.entrySet()) {
            for (String value : entry.getValue()) {
                if (value != null) {
                    String decodedValue = URLDecoder.decode(value, StandardCharsets.UTF_8);
                    if (SENSITIVE_PATTERNS.stream().anyMatch(
                            pattern -> decodedValue.toLowerCase().contains(pattern))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 检查路径中是否包含敏感内容
     */
    private boolean hasSensitiveContentInPath(String path) {
        if (path != null) {
            String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8);
            return SENSITIVE_PATTERNS.stream().anyMatch(
                    pattern -> decodedPath.toLowerCase().contains(pattern));
        }
        return false;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 2; // 在LoggingFilter之后，TraceFilter之后执行
    }
}