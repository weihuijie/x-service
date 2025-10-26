package com.x.api.gateway.service.client;

import com.google.protobuf.Message;
import com.x.common.base.R;
import com.x.common.base.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Dubbo服务客户端基类 - 提供通用的服务调用功能
 * @param <T> gRPC服务接口类型
 */
public abstract class BaseDubboServiceClient<T> {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 执行操作 - 通用方法，将操作委托给具体服务内部处理
     * @param operation 操作名称
     * @param params 参数
     * @param serviceStub gRPC服务桩
     * @param serviceName 服务名称（用于日志）
     * @return 执行结果
     */
    protected R<Map<String, Object>> executeOperation(
            String operation, 
            Map<String, Object> params, 
            T serviceStub, 
            String serviceName) {
        try {
            logger.debug("Calling {} service executeOperation: {}", serviceName, operation);
            
            // 构建请求对象
            com.google.protobuf.GeneratedMessageV3.Builder<?> requestBuilder = createRequestBuilder(operation, params);
            
            // 调用服务
            Object response = callService(serviceStub, requestBuilder.build());
            
            // 转换响应
            Map<String, Object> data = extractDataFromResponse(response);
            boolean success = isResponseSuccessful(response);
            String message = getResponseMessage(response);
            
            if (success) {
                return R.data(data, message);
            } else {
                return R.fail(ResultCode.FAILURE.getCode(), message);
            }
        } catch (Exception e) {
            logger.error("Failed to call {} service executeOperation: {}", serviceName, e.getMessage(), e);
            return R.fail(ResultCode.INTERNAL_SERVER_ERROR, "Failed to execute operation: " + e.getMessage());
        }
    }

    /**
     * 创建请求构建器
     * @param operation 操作名称
     * @param params 参数
     * @return 请求构建器
     */
    protected abstract com.google.protobuf.GeneratedMessageV3.Builder<?> createRequestBuilder(String operation, Map<String, Object> params);

    /**
     * 调用服务
     * @param serviceStub 服务桩
     * @param request 请求对象
     * @return 响应对象
     */
    protected abstract Object callService(T serviceStub, Message request);

    /**
     * 从响应中提取数据
     * @param response 响应对象
     * @return 数据映射
     */
    protected abstract Map<String, Object> extractDataFromResponse(Object response);

    /**
     * 检查响应是否成功
     * @param response 响应对象
     * @return 是否成功
     */
    protected abstract boolean isResponseSuccessful(Object response);

    /**
     * 获取响应消息
     * @param response 响应对象
     * @return 响应消息
     */
    protected abstract String getResponseMessage(Object response);
}