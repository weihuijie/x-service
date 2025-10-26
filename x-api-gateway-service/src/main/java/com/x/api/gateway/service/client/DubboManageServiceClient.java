package com.x.api.gateway.service.client;

import com.google.protobuf.Message;
import com.x.common.base.R;
import com.x.grpc.manage.DubboManageServiceGrpc;
import com.x.grpc.manage.ExecuteOperationRequest;
import com.x.grpc.manage.ExecuteOperationResponse;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Dubbo管理服务客户端 - 使用Dubbo调用管理服务的gRPC接口
 */
@Component
public class DubboManageServiceClient extends BaseDubboServiceClient<DubboManageServiceGrpc.IManageService> {

    // 完善DubboReference配置，添加timeout、retries、cluster和loadbalance等参数
    @DubboReference(
            version = "1.0.0",
            check = false,
            timeout = 3000,            // 超时时间3秒
            retries = 0,               // 不重试，避免重复操作
            cluster = "failfast",      // 快速失败
            loadbalance = "roundrobin" // 轮询负载均衡
    )
    private DubboManageServiceGrpc.IManageService manageService;

    /**
     * 执行操作 - 通用方法，将操作委托给管理服务内部处理
     * @param operation 操作名称
     * @param params 参数
     * @return 执行结果
     */
    public R<Map<String, Object>> executeOperation(String operation, Map<String, Object> params) {
        return super.executeOperation(operation, params, manageService, "manage");
    }

    @Override
    protected com.google.protobuf.GeneratedMessageV3.Builder<?> createRequestBuilder(String operation, Map<String, Object> params) {
        ExecuteOperationRequest.Builder requestBuilder = ExecuteOperationRequest.newBuilder()
                .setOperation(operation);
        
        // 添加参数
        params.forEach((key, value) -> {
            if (value != null) {
                requestBuilder.putParams(key, value.toString());
            }
        });
        
        return requestBuilder;
    }

    @Override
    protected Object callService(DubboManageServiceGrpc.IManageService serviceStub, Message request) {
        return serviceStub.executeOperation((ExecuteOperationRequest) request);
    }

    @Override
    protected Map<String, Object> extractDataFromResponse(Object response) {
        Map<String, String> originalData = ((com.x.grpc.device.ExecuteOperationResponse) response).getDataMap();
        return new HashMap<>(originalData);
    }

    @Override
    protected boolean isResponseSuccessful(Object response) {
        return ((ExecuteOperationResponse) response).getSuccess();
    }

    @Override
    protected String getResponseMessage(Object response) {
        return ((ExecuteOperationResponse) response).getMessage();
    }

    // 通用的Dubbo服务调用与错误处理
    private <T> T invokeWithErrorHandling(
            DubboServiceCall<T> call,
            String operation,
            FallbackProvider<T> fallbackProvider) {
        
        try {
            return call.call();
        } catch (Exception e) {
            logger.error("Failed to call manage service {}: {}", operation, e.getMessage(), e);
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