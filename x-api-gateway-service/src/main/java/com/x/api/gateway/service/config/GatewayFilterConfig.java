package com.x.api.gateway.service.config;

import com.x.api.gateway.service.filter.CustomAddResponseHeaderGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关过滤器配置类
 * 用于注册自定义的网关过滤器工厂
 */
@Configuration
public class GatewayFilterConfig {

    /**
     * 注册自定义的添加响应头过滤器工厂
     * @return 自定义过滤器工厂实例
     */
    @Bean
    public CustomAddResponseHeaderGatewayFilterFactory customAddResponseHeaderGatewayFilterFactory() {
        return new CustomAddResponseHeaderGatewayFilterFactory();
    }
    
    /**
     * 用户限流键解析器
     * 基于用户标识进行限流
     * @return KeyResolver实例
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return new KeyResolver() {
            @Override
            public Mono<String> resolve(ServerWebExchange exchange) {
                // 从请求中获取用户标识，这里简单使用主机名作为示例
                // 实际项目中可以从请求头、JWT token等获取用户标识
                return Mono.just(exchange.getRequest().getRemoteAddress().getHostName());
            }
        };
    }
}