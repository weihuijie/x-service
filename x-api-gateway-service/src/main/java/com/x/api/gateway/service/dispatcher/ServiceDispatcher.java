package com.x.api.gateway.service.dispatcher;

import com.x.common.base.R;
import com.x.common.base.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Optional;

/**
 * 服务分发器 - 在请求拦截后根据路径和请求参数将请求分发到相应的Dubbo服务
 * 负责处理不同服务的调用逻辑，实现请求的路由分发
 */
@Slf4j
@Component
public class ServiceDispatcher {

    // 自定义线程池，避免使用默认的ForkJoinPool
    private static final ExecutorService serviceExecutor = Executors.newFixedThreadPool(
            Math.max(4, Runtime.getRuntime().availableProcessors()),
            r -> {
                Thread t = new Thread(r, "service-dispatcher-");
                t.setDaemon(true);
                return t;
            }
    );

    private final ServiceOperationRegistry operationRegistry;

    public ServiceDispatcher(ServiceOperationRegistry operationRegistry) {
        this.operationRegistry = operationRegistry;
    }

    /**
     * 通用服务请求分发方法
     * @param serviceType 服务类型
     * @param operation 操作类型
     * @param params 请求参数
     * @return 处理结果
     */
    public CompletableFuture<R<Map<String, Object>>> dispatchRequest(String serviceType, String operation, Map<String, Object> params) {
        return CompletableFuture.supplyAsync(() -> {
            log.debug("Dispatching request: serviceType={}, operation={}", serviceType, operation);
            
            try {
                // 查找对应的服务操作
                Optional<ServiceOperation> serviceOperation = operationRegistry.findOperation(serviceType, operation);
                
                if (serviceOperation.isPresent()) {
                    // 执行服务操作
                    return serviceOperation.get().execute(params);
                } else {
                    // 未找到对应的服务操作
                    return R.fail(ResultCode.FAILURE, "Unsupported operation: " + operation + " for service: " + serviceType);
                }
            } catch (Exception e) {
                return handleException(serviceType, operation, e);
            }
        }, serviceExecutor);
    }

    // 通用异常处理
    private R<Map<String, Object>> handleException(String serviceType, String operation, Exception e) {
        log.error("Error dispatching {} request [{}]: {}", serviceType, operation, e.getMessage(), e);
        
        if (e instanceof IllegalArgumentException) {
            return R.fail(ResultCode.PARAM_VALID_ERROR, e.getMessage());
        } else {
            return R.fail(ResultCode.INTERNAL_SERVER_ERROR, "Error processing request: " + e.getMessage());
        }
    }
}