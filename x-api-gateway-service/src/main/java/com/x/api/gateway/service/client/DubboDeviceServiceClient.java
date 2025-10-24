package com.x.api.gateway.service.client;

import com.x.grpc.device.*;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.x.common.base.R;
import com.x.common.base.ResultCode;

import java.util.Map;
import java.util.HashMap;

/**
 * Dubbo设备服务客户端 - 使用Dubbo调用设备管理服务的gRPC接口
 */
@Component
public class DubboDeviceServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(DubboDeviceServiceClient.class);

    // 完善DubboReference配置，添加timeout、retries、cluster和loadbalance等参数
    @DubboReference(
            version = "1.0.0",
            check = false,
            timeout = 5000,            // 超时时间5秒，比认证服务稍长
            retries = 0,               // 不重试，避免重复操作
            cluster = "failfast",      // 快速失败
            loadbalance = "leastactive" // 最小活跃数负载均衡
    )
    private DubboDeviceServiceGrpc.IDeviceService deviceService;

    /**
     * 执行操作 - 通用方法，将操作委托给设备服务内部处理
     * @param operation 操作名称
     * @param params 参数
     * @return 执行结果
     */
    public R<Map<String, Object>> executeOperation(String operation, Map<String, Object> params) {
        try {
            logger.debug("Calling device service executeOperation: {}", operation);
            
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
            ExecuteOperationResponse response = deviceService.executeOperation(requestBuilder.build());
            // 转换响应
            Map<String, Object> data = new HashMap<>(response.getDataMap());
            
            if (response.getSuccess()) {
                R.success(ResultCode.SUCCESS, response.getMessage());
            } else {
                response.getMessage();
            }
            return R.data(data);
        } catch (Exception e) {
            logger.error("Failed to call device service executeOperation: {}", e.getMessage(), e);
            return R.fail(ResultCode.INTERNAL_SERVER_ERROR, "Failed to execute operation: " + e.getMessage());
        }
    }

    // 通用的Dubbo服务调用与错误处理
    private <T> T invokeWithErrorHandling(
            DubboServiceCall<T> call,
            String operation,
            FallbackProvider<T> fallbackProvider) {
        
        // 简单的服务熔断判断 - 实际项目中可以集成Sentinel或Resilience4j
        // 注意：在新版本Dubbo中，RpcStatus的API已更改，此处暂时移除相关逻辑
        // 可以在后续版本中使用Sentinel等组件替代熔断功能
        
        try {
            return call.call();
        } catch (Exception e) {
            logger.error("Failed to call device service {}: {}", operation, e.getMessage(), e);
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