package com.x.api.gateway.service.dispatcher.operation;

import com.x.api.gateway.service.client.DubboManageServiceClient;
import com.x.api.gateway.service.dispatcher.ServiceOperation;
import com.x.common.base.R;
import com.x.common.base.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 管理服务操作实现
 * 将具体业务逻辑委托给管理服务处理
 */
@Slf4j
@Component
public class ManageServiceOperation implements ServiceOperation {
    
    private final DubboManageServiceClient manageServiceClient;
    
    public ManageServiceOperation(DubboManageServiceClient manageServiceClient) {
        this.manageServiceClient = manageServiceClient;
    }
    
    @Override
    public R<Map<String, Object>> execute(Map<String, Object> params) {
        // 将参数直接传递给管理服务处理，具体业务逻辑在管理服务内部实现
        try {
            // 根据参数中的操作类型执行对应的方法
            String operation = (String) params.get("operation");
            if (operation == null) {
                return R.fail(ResultCode.PARAM_VALID_ERROR, "Missing operation parameter");
            }
            
            // 直接将操作和参数传递给管理服务，由管理服务内部处理具体逻辑
            return manageServiceClient.executeOperation(operation, params);
        } catch (Exception e) {
            log.error("Error executing manage operation: {}", e.getMessage(), e);
            return R.fail(ResultCode.INTERNAL_SERVER_ERROR, "Error executing manage operation: " + e.getMessage());
        }
    }
    
    @Override
    public String getServiceType() {
        return "manage";
    }
    
    @Override
    public String getOperationName() {
        return "default";
    }
}