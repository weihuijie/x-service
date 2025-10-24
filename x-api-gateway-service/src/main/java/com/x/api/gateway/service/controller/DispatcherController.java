package com.x.api.gateway.service.controller;

import com.x.api.gateway.service.dispatcher.ServiceDispatcher;
import com.x.common.base.R;
import com.x.common.base.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 请求分发控制器
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class DispatcherController {

    private final ServiceDispatcher serviceDispatcher;

    public DispatcherController(ServiceDispatcher serviceDispatcher) {
        this.serviceDispatcher = serviceDispatcher;
    }

    /**
     * 通用分发接口 - 根据路径和参数分发请求到不同服务
     * 这个示例展示了如何在拦截请求后进行智能路由分发
     */
    @PostMapping("/dispatch/{serviceType}/{operation}")
    public CompletableFuture<ResponseEntity<R<Map<String, Object>>>> dispatchRequest(
            @PathVariable String serviceType,  // 服务类型
            @PathVariable String operation,    // 操作类型
            @RequestBody Map<String, Object> params) {  // 请求参数

        log.info("接受到请求: serviceType={}, operation={}", serviceType, operation);

        // 使用新的通用分发方法
        CompletableFuture<R<Map<String, Object>>> resultFuture = 
            serviceDispatcher.dispatchRequest(serviceType, operation, params);

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