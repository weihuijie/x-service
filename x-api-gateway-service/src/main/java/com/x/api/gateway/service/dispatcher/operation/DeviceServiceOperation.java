package com.x.api.gateway.service.dispatcher.operation;

import com.x.api.gateway.service.client.DubboDeviceServiceClient;
import com.x.api.gateway.service.dispatcher.ServiceOperation;
import com.x.common.base.R;
import com.x.common.base.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 设备服务操作实现
 * 将具体业务逻辑委托给设备服务处理
 */
@Slf4j
@Component
public class DeviceServiceOperation implements ServiceOperation {
    
    private final DubboDeviceServiceClient deviceServiceClient;
    
    public DeviceServiceOperation(DubboDeviceServiceClient deviceServiceClient) {
        this.deviceServiceClient = deviceServiceClient;
    }
    
    @Override
    public R<Map<String, Object>> execute(Map<String, Object> params) {
        // 将参数直接传递给设备服务处理，具体业务逻辑在设备服务内部实现
        try {
            // 根据参数中的操作类型执行对应的方法
            String operation = (String) params.get("operation");
            if (operation == null) {
                return R.fail(ResultCode.PARAM_VALID_ERROR, "Missing operation parameter");
            }
            
            // 直接将操作和参数传递给设备服务，由设备服务内部处理具体逻辑
            return deviceServiceClient.executeOperation(operation, params);
        } catch (Exception e) {
            log.error("Error executing device operation: {}", e.getMessage(), e);
            return R.fail(ResultCode.INTERNAL_SERVER_ERROR, "Error executing device operation: " + e.getMessage());
        }
    }
    
    @Override
    public String getServiceType() {
        return "device";
    }
    
    @Override
    public String getOperationName() {
        return "default";
    }
}