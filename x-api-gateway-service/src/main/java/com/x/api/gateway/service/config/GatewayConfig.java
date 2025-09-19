package com.x.api.gateway.service.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Configuration
public class GatewayConfig {

    /**
     * 用户限流Key解析器
     * 根据用户ID或请求参数中的用户标识进行限流
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            // 从请求参数中获取用户标识，也可以从请求头中获取
            String userId = exchange.getRequest().getQueryParams().getFirst("userId");
            if (userId == null) {
                // 如果没有用户标识，使用IP地址
                return Mono.just(Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress());
            }
            return Mono.just(userId);
        };
    }

    /**
     * 路径限流Key解析器
     * 根据请求路径进行限流
     */
    @Bean
    public KeyResolver pathKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getURI().getPath());
    }

    /**
     * IP限流Key解析器
     * 根据客户端IP地址进行限流
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
                Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress()
        );
    }
}