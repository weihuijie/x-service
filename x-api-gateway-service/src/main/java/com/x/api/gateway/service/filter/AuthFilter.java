package com.x.api.gateway.service.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.core.Ordered;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
public class AuthFilter implements Ordered {
    
    // 不需要认证的路径
    private static final List<String> WHITE_LIST = Arrays.asList(
        "/auth/login",
        "/auth/register",
        "/health",
        "/actuator"
    );
    
    @Autowired
    private RestTemplate restTemplate;
    
    public Mono<Void> filter(ServerWebExchange exchange) {
        String path = exchange.getRequest().getURI().getPath();
        
        // 检查是否在白名单中
        if (isWhiteListed(path)) {
            return exchange.getResponse().setComplete(); // 允许通过
        }
        
        // 检查请求头中是否包含Authorization
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        // 如果Authorization为空，则返回401未授权
        if (authHeader == null || authHeader.isEmpty()) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        
        // 验证token有效性
        if (!validateToken(authHeader)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        
        // token有效，允许通过
        return exchange.getResponse().setComplete();
    }
    
    private boolean isWhiteListed(String path) {
        return WHITE_LIST.stream().anyMatch(path::startsWith);
    }
    
    private boolean validateToken(String authHeader) {
        if (!authHeader.startsWith("Bearer ")) {
            return false;
        }
        
        String token = authHeader.substring(7);
        
        try {
            // 在实际应用中，这里会调用auth-service验证token
            // 示例中简化处理，始终返回true
            // ResponseEntity<String> response = restTemplate.postForEntity(
            //     "http://auth-service/auth/validate", 
            //     createTokenRequest(token), 
            //     String.class
            // );
            // return response.getStatusCode().is2xxSuccessful();
            
            // 模拟验证过程
            return token.length() > 10; // 简单验证token长度
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public int getOrder() {
        return -100; // 设置过滤器顺序，值越小优先级越高
    }
}