package com.x.api.gateway.service.config;

import com.x.common.base.R;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 全局错误Web异常处理器
 * 处理WebFlux中的所有未捕获异常，提供统一的错误响应格式
 */
@Component
@Order(-2)
public class GlobalErrorWebExceptionHandler extends AbstractErrorWebExceptionHandler {

    public GlobalErrorWebExceptionHandler(
            ErrorAttributes errorAttributes,
            WebProperties webProperties,
            ApplicationContext applicationContext,
            ServerCodecConfigurer configurer) {
        super(errorAttributes, webProperties.getResources(), applicationContext);
        this.setMessageWriters(configurer.getWriters());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Map<String, Object> errorPropertiesMap = getErrorAttributes(request, ErrorAttributeOptions.defaults());
        
        HttpStatus status = determineStatus(errorPropertiesMap);
        String message = determineMessage(errorPropertiesMap);
        
        R<?> response = R.fail(status.value(), message);
        
        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(response));
    }

    private HttpStatus determineStatus(Map<String, Object> errorPropertiesMap) {
        Integer statusCode = (Integer) errorPropertiesMap.get("status");
        if (statusCode != null) {
            try {
                return HttpStatus.valueOf(statusCode);
            } catch (Exception ex) {
                // Ignore and return default
            }
        }
        
        // 默认返回500错误
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String determineMessage(Map<String, Object> errorPropertiesMap) {
        // 检查是否是资源未找到错误
        String path = (String) errorPropertiesMap.get("path");
        if (path != null && path.contains("/api/")) {
            return "API接口不存在或服务不可用";
        }
        
        // 获取错误信息
        String message = (String) errorPropertiesMap.get("message");
        if (message == null || message.isEmpty()) {
            message = "服务器内部错误";
        }
        
        // 特殊处理NoResourceFoundException
        if (message.contains("No static resource")) {
            return "请求的资源不存在";
        }
        
        return message;
    }
}