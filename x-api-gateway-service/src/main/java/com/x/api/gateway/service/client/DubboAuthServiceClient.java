package com.x.api.gateway.service.client;

import com.x.grpc.auth.*;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.x.common.base.R;
import com.x.common.base.ResultCode;

import java.util.Map;
import java.util.HashMap;

/**
 * Dubbo认证服务客户端 - 使用Dubbo调用认证服务的gRPC接口
 * 替代之前的Feign客户端，提供更高效的服务调用
 */
@Component
public class DubboAuthServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(DubboAuthServiceClient.class);

    // 完善DubboReference配置，添加timeout、retries、cluster和loadbalance等参数
    @DubboReference(
            version = "1.0.0",
            check = false,
            timeout = 3000,            // 超时时间3秒
            retries = 0,               // 不重试，避免重复操作
            cluster = "failfast",      // 快速失败
            loadbalance = "consistenthash" // 一致性哈希负载均衡
    )
    private DubboAuthServiceGrpc.IAuthService authService;

    /**
     * 执行操作 - 通用方法，将操作委托给认证服务内部处理
     * @param operation 操作名称
     * @param params 参数
     * @return 执行结果
     */
    public R<Map<String, Object>> executeOperation(String operation, Map<String, Object> params) {
        try {
            logger.debug("Calling auth service executeOperation: {}", operation);
            
            // 构建请求对象
            ExecuteOperationRequest.Builder requestBuilder = ExecuteOperationRequest.newBuilder()
                    .setOperation(operation);
            
            // 添加参数
            params.forEach((key, value) -> {
                if (value != null) {
                    requestBuilder.putParams(key, value.toString());
                }
            });
            
            // 调用服务
            ExecuteOperationResponse response = authService.executeOperation(requestBuilder.build());
            
            // 转换响应
            Map<String, Object> data = new HashMap<>(response.getDataMap());
            if (response.getSuccess()) {
                R.success(ResultCode.SUCCESS, response.getMessage());
            } else {
                response.getMessage();
            }
            return R.data(data);
        } catch (Exception e) {
            logger.error("Failed to call auth service executeOperation: {}", e.getMessage(), e);
            return R.fail(ResultCode.INTERNAL_SERVER_ERROR, "Failed to execute operation: " + e.getMessage());
        }
    }

    // 通用的Dubbo服务调用与错误处理
    private <T> T invokeWithErrorHandling(
            DubboServiceCall<T> call,
            String operation,
            FallbackProvider<T> fallbackProvider) {
        
        try {
            return call.call();
        } catch (Exception e) {
            logger.error("Failed to call auth service {}: {}", operation, e.getMessage(), e);
            return fallbackProvider.getFallback();
        }
    }

    // 函数式接口定义
    private interface DubboServiceCall<T> {
        T call();
    }

    private interface FallbackProvider<T> {
        T getFallback();
    }
}