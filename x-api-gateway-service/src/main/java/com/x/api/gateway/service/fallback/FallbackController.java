package com.x.api.gateway.service.fallback;

import com.x.common.base.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * 熔断降级控制器 - 处理服务不可用时的降级逻辑
 */
@Slf4j
@RestController
public class FallbackController {

    /**
     * 默认降级处理方法
     */
    @RequestMapping("/fallback")
    public Mono<R<Map<String, Object>>> fallback() {
        log.warn("服务调用触发熔断降级");
        
        Map<String, Object> data = new HashMap<>();
        data.put("message", "Service currently unavailable, please try again later");
        data.put("code", "SERVICE_UNAVAILABLE");
        
        R<Map<String, Object>> response = R.fail(503, "Service Unavailable");
        response.setData(data);
        
        return Mono.just(response);
    }

    /**
     * 管理服务降级处理方法
     */
    @RequestMapping("/fallback/manage")
    public Mono<R<Map<String, Object>>> manageServiceFallback() {
        log.warn("管理服务调用触发熔断降级");
        
        Map<String, Object> data = new HashMap<>();
        data.put("message", "Manage service currently unavailable, please try again later");
        data.put("code", "MANAGE_SERVICE_UNAVAILABLE");
        
        R<Map<String, Object>> response = R.fail(503, "Manage Service Unavailable");
        response.setData(data);
        
        return Mono.just(response);
    }
}