package com.x.api.gateway.service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器 - 提供统一的网关健康检查端点
 */
@RestController
public class HealthController {

    /**
     * 网关健康检查接口
     * @return 健康状态信息
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", "api-gateway-service");
        result.put("timestamp", System.currentTimeMillis());
        
        Map<String, Object> details = new HashMap<>();
        details.put("version", "1.0.0");
        details.put("description", "API Gateway Service for X-Service IoT Platform");
        result.put("details", details);
        
        return ResponseEntity.ok(result);
    }
}