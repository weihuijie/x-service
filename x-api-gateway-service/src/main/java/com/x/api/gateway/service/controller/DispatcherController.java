package com.x.api.gateway.service.controller;

import com.x.api.gateway.service.dispatcher.ServiceDispatcher;
import com.x.common.base.R;
import com.x.common.base.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 请求分发控制器 - 展示如何在API网关中拦截请求后分发到不同的Dubbo/gRPC服务
 * 这个控制器作为网关的一部分，接收外部HTTP请求并转发到内部的Dubbo服务
 */
@RestController
@RequestMapping("/api/gateway")
public class DispatcherController {

    private static final Logger logger = LoggerFactory.getLogger(DispatcherController.class);

    @Autowired
    private ServiceDispatcher serviceDispatcher;

    /**
     * 通用分发接口 - 根据路径和参数分发请求到不同服务
     * 这个示例展示了如何在拦截请求后进行智能路由分发
     */
    @PostMapping("/dispatch/{serviceType}/{operation}")
    public CompletableFuture<ResponseEntity<R<Map<String, Object>>>> dispatchRequest(
            @PathVariable String serviceType,  // 服务类型(auth/device等)
            @PathVariable String operation,    // 操作类型
            @RequestBody Map<String, Object> params) {  // 请求参数

        logger.info("Received dispatch request: serviceType={}, operation={}", serviceType, operation);

        // 根据服务类型分发请求到不同的处理方法
        CompletableFuture<R<Map<String, Object>>> resultFuture;
        switch (serviceType.toLowerCase()) {
            case "auth":
                resultFuture = serviceDispatcher.dispatchAuthRequest(operation, params);
                break;
            case "device":
                resultFuture = serviceDispatcher.dispatchDeviceRequest(operation, params);
                break;
            default:
                resultFuture = CompletableFuture.completedFuture(R.fail(ResultCode.FAILURE, "Unsupported service type: " + serviceType));
        }

        // 处理异步结果并返回响应
        return resultFuture.thenApply(result -> {
            HttpStatus status = result.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
            return new ResponseEntity<>(result, status);
        }).exceptionally(e -> {
            logger.error("Error processing dispatch request: {}", e.getMessage(), e);
            R<Map<String, Object>> errorResponse = R.fail(ResultCode.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        });
    }

    /**
     * 认证服务特定接口示例 - 用户登录
     * 展示如何将HTTP请求转换为Dubbo服务调用
     */
    @PostMapping("/auth/login")
    public CompletableFuture<ResponseEntity<R<Map<String, Object>>>> login(
            @RequestBody LoginRequest loginRequest) {

        logger.info("Received login request for user: {}", loginRequest.getUsername());
        Map<String, Object> params = new HashMap<>();
        params.put("username", loginRequest.getUsername());
        params.put("password", loginRequest.getPassword());
        
        return serviceDispatcher.dispatchAuthRequest("login", params)
                .thenApply(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .exceptionally(e -> {
                    logger.error("Error processing login request: {}", e.getMessage(), e);
                    return new ResponseEntity<>(R.fail(ResultCode.INTERNAL_SERVER_ERROR, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    /**
     * 设备服务特定接口示例 - 获取设备列表
     * 展示如何处理带查询参数的请求
     */
    @GetMapping("/devices")
    public CompletableFuture<ResponseEntity<R<Map<String, Object>>>> listDevices(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {

        logger.info("Received list devices request: page={}, size={}, status={}", page, size, status);
        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("size", size);
        if (status != null) {
            params.put("status", status);
        }
        
        return serviceDispatcher.dispatchDeviceRequest("listDevices", params)
                .thenApply(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .exceptionally(e -> {
                    logger.error("Error processing list devices request: {}", e.getMessage(), e);
                    return new ResponseEntity<>(R.fail(ResultCode.INTERNAL_SERVER_ERROR, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    /**
     * 登录请求参数类
     */
    static class LoginRequest {
        private String username;
        private String password;
        
        // Getters and setters
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
    }
}