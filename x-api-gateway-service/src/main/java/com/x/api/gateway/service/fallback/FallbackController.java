package com.x.api.gateway.service.fallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 熔断降级控制器 - 处理服务不可用或响应超时的情况
 */
@RestController
public class FallbackController {

    private static final Logger logger = LoggerFactory.getLogger(FallbackController.class);
    
    /**
     * 默认降级处理
     */
    @RequestMapping("/fallback")
    public ResponseEntity<Map<String, Object>> defaultFallback() {
        logger.warn("Service fallback triggered for default route");
        return createFallbackResponse("服务暂时不可用，请稍后再试", HttpStatus.SERVICE_UNAVAILABLE);
    }
    
    /**
     * 管理服务降级处理
     */
    @RequestMapping("/fallback/manage")
    public ResponseEntity<Map<String, Object>> manageServiceFallback() {
        logger.warn("Service fallback triggered for manage-service");
        return createFallbackResponse("管理服务暂时不可用，请稍后再试", HttpStatus.SERVICE_UNAVAILABLE);
    }
    
    /**
     * 设备服务降级处理
     */
    @RequestMapping("/fallback/device")
    public ResponseEntity<Map<String, Object>> deviceServiceFallback() {
        logger.warn("Service fallback triggered for device-service");
        return createFallbackResponse("设备服务暂时不可用，请稍后再试", HttpStatus.SERVICE_UNAVAILABLE);
    }
    
    /**
     * 认证服务降级处理
     */
    @RequestMapping("/fallback/auth")
    public ResponseEntity<Map<String, Object>> authServiceFallback() {
        logger.warn("Service fallback triggered for auth-service");
        return createFallbackResponse("认证服务暂时不可用，请稍后再试", HttpStatus.SERVICE_UNAVAILABLE);
    }
    
    /**
     * 文件服务降级处理
     */
    @RequestMapping("/fallback/file")
    public ResponseEntity<Map<String, Object>> fileServiceFallback() {
        logger.warn("Service fallback triggered for file-service");
        return createFallbackResponse("文件服务暂时不可用，请稍后再试", HttpStatus.SERVICE_UNAVAILABLE);
    }
    
    /**
     * 告警服务降级处理
     */
    @RequestMapping("/fallback/alert")
    public ResponseEntity<Map<String, Object>> alertServiceFallback() {
        logger.warn("Service fallback triggered for alert-service");
        return createFallbackResponse("告警服务暂时不可用，请稍后再试", HttpStatus.SERVICE_UNAVAILABLE);
    }
    
    /**
     * 数据服务降级处理
     */
    @RequestMapping("/fallback/data")
    public ResponseEntity<Map<String, Object>> dataServiceFallback() {
        logger.warn("Service fallback triggered for data-service");
        return createFallbackResponse("数据服务暂时不可用，请稍后再试", HttpStatus.SERVICE_UNAVAILABLE);
    }
    
    /**
     * 创建统一的降级响应
     */
    private ResponseEntity<Map<String, Object>> createFallbackResponse(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", status.value());
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        response.put("status", status.getReasonPhrase());
        
        return new ResponseEntity<>(response, status);
    }
}