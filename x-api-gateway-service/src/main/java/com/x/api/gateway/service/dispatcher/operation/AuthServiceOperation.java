package com.x.api.gateway.service.dispatcher.operation;

import com.x.api.gateway.service.client.DubboAuthServiceClient;
import com.x.api.gateway.service.dispatcher.ServiceOperation;
import com.x.common.base.R;
import com.x.common.base.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 认证服务操作实现
 * 将具体业务逻辑委托给认证服务处理
 */
@Slf4j
@Component
public class AuthServiceOperation implements ServiceOperation {
    
    private final DubboAuthServiceClient authServiceClient;
    
    public AuthServiceOperation(DubboAuthServiceClient authServiceClient) {
        this.authServiceClient = authServiceClient;
    }
    
    @Override
    public R<Map<String, Object>> execute(Map<String, Object> params) {
        // 将参数直接传递给认证服务处理，具体业务逻辑在认证服务内部实现
        try {
            // 根据参数中的操作类型执行对应的方法
            String operation = (String) params.get("operation");
            if (operation == null) {
                return R.fail(ResultCode.PARAM_VALID_ERROR, "Missing operation parameter");
            }
            
            // 直接将操作和参数传递给认证服务，由认证服务内部处理具体逻辑
            return authServiceClient.executeOperation(operation, params);
        } catch (Exception e) {
            log.error("Error executing auth operation: {}", e.getMessage(), e);
            return R.fail(ResultCode.INTERNAL_SERVER_ERROR, "Error executing auth operation: " + e.getMessage());
        }
    }
    
    @Override
    public String getServiceType() {
        return "auth";
    }
    
    @Override
    public String getOperationName() {
        return "default";
    }
}