package com.x.api.gateway.service.filter;

import com.x.api.gateway.service.client.AuthServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 认证过滤器 - 实现GlobalFilter接口，用于全局请求认证
 */
@Component
public class AuthFilter implements GlobalFilter, Ordered {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthFilter.class);
    
    // 不需要认证的路径
    private static final List<String> WHITE_LIST = Arrays.asList(
        "/auth/login",
        "/auth/register",
        "/health",
        "/actuator",
        "/swagger-ui",
        "/v3/api-docs"
    );
    
    @Autowired
    private AuthServiceClient authServiceClient;
    
    /**
     * 实现GlobalFilter接口的filter方法
     * 处理请求认证逻辑
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        
        // 检查是否在白名单中
        if (isWhiteListed(path)) {
            return chain.filter(exchange); // 允许通过，继续过滤器链
        }
        
        // 检查请求头中是否包含Authorization
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        // 如果Authorization为空，则返回401未授权
        if (authHeader == null || authHeader.isEmpty()) {
            logger.warn("Authorization header is missing for path: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        
        // 验证token格式
        if (!authHeader.startsWith("Bearer ")) {
            logger.warn("Invalid authorization format for path: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        
        String token = authHeader.substring(7);
        
        try {
            // 使用CompletableFuture异步调用认证服务，设置超时时间
            CompletableFuture<AuthServiceClient.ValidateTokenResponse> future = CompletableFuture.supplyAsync(() -> {
                return authServiceClient.validateToken(new AuthServiceClient.TokenRequest(token));
            });
            
            // 处理异步结果，设置3秒超时
            AuthServiceClient.ValidateTokenResponse response = future.get(3, TimeUnit.SECONDS);
            
            if (!response.isValid()) {
                logger.warn("Token validation failed for path: {}, reason: {}", path, response.getMessage());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            
            // token有效，将用户信息添加到请求头中，继续过滤器链
            ServerHttpRequest request = exchange.getRequest().mutate()
                    .header("X-User-Id", response.getUserId())
                    .header("X-Username", response.getUsername())
                    .header("X-User-Info", createUserInfoHeader(response))
                    .build();
            
            logger.debug("Token validation successful for user: {} accessing path: {}", 
                    response.getUsername(), path);
            
            return chain.filter(exchange.mutate().request(request).build());
        } catch (InterruptedException e) {
            logger.error("Token validation interrupted for path: {}", path, e);
            Thread.currentThread().interrupt(); // 恢复中断状态
            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return exchange.getResponse().setComplete();
        } catch (ExecutionException e) {
            logger.error("Error validating token for path: {}", path, e.getCause());
            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return exchange.getResponse().setComplete();
        } catch (TimeoutException e) {
            logger.error("Token validation timeout for path: {}", path, e);
            exchange.getResponse().setStatusCode(HttpStatus.REQUEST_TIMEOUT);
            return exchange.getResponse().setComplete();
        } catch (Exception e) {
            logger.error("Unexpected error validating token for path: {}", path, e);
            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return exchange.getResponse().setComplete();
        }
    }
    
    /**
     * 检查路径是否在白名单中
     */
    private boolean isWhiteListed(String path) {
        return WHITE_LIST.stream().anyMatch(path::startsWith);
    }
    
    /**
     * 创建用户信息请求头
     */
    private String createUserInfoHeader(AuthServiceClient.ValidateTokenResponse response) {
        // 实际应用中可能需要更复杂的用户信息
        return "{\"userId\":\"" + response.getUserId() + "\",\"username\":\"" + response.getUsername() + "\"}";
    }
    
    /**
     * 设置过滤器顺序，值越小优先级越高
     */
    @Override
    public int getOrder() {
        return -100; // 高优先级，确保认证在其他过滤器之前执行
    }
}