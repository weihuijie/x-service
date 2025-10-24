package com.x.api.gateway.service.dispatcher;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 服务操作注册中心 - 管理所有可用的服务操作
 * 支持动态注册和查找服务操作，减少硬编码
 */
@Component
public class ServiceOperationRegistry {
    
    // 服务操作映射: serviceType -> operationName -> ServiceOperation
    private final Map<String, Map<String, ServiceOperation>> serviceOperations = new ConcurrentHashMap<>();
    
    /**
     * 注册服务操作
     * @param operation 服务操作实例
     */
    public void registerOperation(ServiceOperation operation) {
        String serviceType = operation.getServiceType().toLowerCase();
        String operationName = operation.getOperationName().toLowerCase();
        
        serviceOperations
            .computeIfAbsent(serviceType, k -> new ConcurrentHashMap<>())
            .put(operationName, operation);
    }
    
    /**
     * 查找服务操作
     * @param serviceType 服务类型
     * @param operationName 操作名称
     * @return 服务操作实例，如果未找到则返回空Optional
     */
    public Optional<ServiceOperation> findOperation(String serviceType, String operationName) {
        Map<String, ServiceOperation> operations = serviceOperations.get(serviceType.toLowerCase());
        if (operations != null) {
            ServiceOperation operation = operations.get(operationName.toLowerCase());
            if (operation != null) {
                return Optional.of(operation);
            }
        }
        return Optional.empty();
    }
    
    /**
     * 检查服务操作是否存在
     * @param serviceType 服务类型
     * @param operationName 操作名称
     * @return 是否存在
     */
    public boolean containsOperation(String serviceType, String operationName) {
        return findOperation(serviceType, operationName).isPresent();
    }
}