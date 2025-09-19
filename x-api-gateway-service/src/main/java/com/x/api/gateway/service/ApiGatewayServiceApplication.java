package com.x.api.gateway.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openFeign.EnableFeignClients;

/**
 * API网关服务入口类
 * 集成Spring Cloud Gateway实现统一的API接入、路由转发、认证授权、限流熔断等功能
 */
@SpringBootApplication
@EnableDiscoveryClient  // 启用服务发现
@EnableFeignClients     // 启用Feign客户端
public class ApiGatewayServiceApplication {
    
    /**
     * 主入口方法
     */
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayServiceApplication.class, args);
    }
}