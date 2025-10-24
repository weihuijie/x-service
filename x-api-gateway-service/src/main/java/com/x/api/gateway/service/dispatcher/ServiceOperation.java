package com.x.api.gateway.service.dispatcher;

import com.x.common.base.R;

import java.util.Map;

/**
 * 服务操作接口 - 定义通用的服务操作规范
 * 通过实现此接口，可以动态注册和执行各种服务操作
 */
public interface ServiceOperation {
    
    /**
     * 执行服务操作
     * @param params 请求参数
     * @return 操作结果
     */
    R<Map<String, Object>> execute(Map<String, Object> params);
    
    /**
     * 获取服务类型
     * @return 服务类型标识
     */
    String getServiceType();
    
    /**
     * 获取操作名称
     * @return 操作名称
     */
    String getOperationName();
}