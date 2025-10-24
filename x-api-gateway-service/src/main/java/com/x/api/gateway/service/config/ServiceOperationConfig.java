package com.x.api.gateway.service.config;

import com.x.api.gateway.service.dispatcher.ServiceOperationRegistry;
import com.x.api.gateway.service.dispatcher.operation.AuthServiceOperation;
import com.x.api.gateway.service.dispatcher.operation.DeviceServiceOperation;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * 服务操作配置类 - 自动注册所有服务操作
 */
@Configuration
public class ServiceOperationConfig {
    
    private final ServiceOperationRegistry operationRegistry;
    private final AuthServiceOperation authServiceOperation;
    private final DeviceServiceOperation deviceServiceOperation;
    
    public ServiceOperationConfig(ServiceOperationRegistry operationRegistry, 
                                  AuthServiceOperation authServiceOperation,
                                  DeviceServiceOperation deviceServiceOperation) {
        this.operationRegistry = operationRegistry;
        this.authServiceOperation = authServiceOperation;
        this.deviceServiceOperation = deviceServiceOperation;
    }
    
    @PostConstruct
    public void registerServiceOperations() {
        // 注册认证服务操作
        operationRegistry.registerOperation(authServiceOperation);
        
        // 注册设备服务操作
        operationRegistry.registerOperation(deviceServiceOperation);
    }
}