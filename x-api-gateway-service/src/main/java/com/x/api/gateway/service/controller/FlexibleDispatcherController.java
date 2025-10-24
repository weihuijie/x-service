package com.x.api.gateway.service.controller;

import com.x.api.gateway.service.dispatcher.ServiceDispatcher;
import com.x.common.base.R;
import com.x.common.base.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 灵活分发控制器 - 提供更灵活的API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/v2")
public class FlexibleDispatcherController {

    private final ServiceDispatcher serviceDispatcher;

    public FlexibleDispatcherController(ServiceDispatcher serviceDispatcher) {
        this.serviceDispatcher = serviceDispatcher;
    }

    /**
     * 通用分发接口 - 根据服务类型和操作类型分发请求
     */
    @PostMapping("/{serviceType}/{operation}")
    public CompletableFuture<ResponseEntity<R<Map<String, Object>>>> dispatchRequest(
            @PathVariable String serviceType,
            @PathVariable String operation,
            @RequestBody Map<String, Object> params) {
        
        log.info("接收到请求: serviceType={}, operation={}", serviceType, operation);
        
        // 添加操作参数到params中
        Map<String, Object> enrichedParams = new HashMap<>(params);
        enrichedParams.put("operation", operation);
        
        // 分发请求
        CompletableFuture<R<Map<String, Object>>> resultFuture = 
            serviceDispatcher.dispatchRequest(serviceType, "default", enrichedParams);

        // 处理异步结果并返回响应
        return resultFuture.thenApply(result -> {
            HttpStatus status = result.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
            return new ResponseEntity<>(result, status);
        }).exceptionally(e -> {
            log.error("请求异常: {}", e.getMessage(), e);
            R<Map<String, Object>> errorResponse = R.fail(ResultCode.INTERNAL_SERVER_ERROR, "请求异常: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        });
    }
    
    /**
     * GET请求分发接口 - 适用于查询类操作
     */
    @GetMapping("/{serviceType}/{operation}")
    public CompletableFuture<ResponseEntity<R<Map<String, Object>>>> dispatchGetRequest(
            @PathVariable String serviceType,
            @PathVariable String operation,
            @RequestParam Map<String, String> params) {
        
        log.info("接收到GET请求: serviceType={}, operation={}", serviceType, operation);
        
        // 转换参数类型并添加操作参数
        Map<String, Object> enrichedParams = new HashMap<>();
        params.forEach((key, value) -> enrichedParams.put(key, value));
        enrichedParams.put("operation", operation);
        
        // 分发请求
        CompletableFuture<R<Map<String, Object>>> resultFuture = 
            serviceDispatcher.dispatchRequest(serviceType, "default", enrichedParams);

        // 处理异步结果并返回响应
        return resultFuture.thenApply(result -> {
            HttpStatus status = result.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
            return new ResponseEntity<>(result, status);
        }).exceptionally(e -> {
            log.error("请求异常: {}", e.getMessage(), e);
            R<Map<String, Object>> errorResponse = R.fail(ResultCode.INTERNAL_SERVER_ERROR, "请求异常: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        });
    }
}