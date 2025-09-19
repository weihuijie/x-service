package com.x.api.gateway.service.handler;

import com.x.common.base.R;
import com.x.common.base.ResultCode;
import com.x.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器 - 统一处理API网关中的所有异常
 * 使用@ControllerAdvice注解捕获所有控制器中的异常
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * 处理参数验证异常 - 如缺少必填参数、参数格式错误等
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<R<?>> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        logger.warn("Bad request: {}", ex.getMessage());
        R<?> response = R.fail(ResultCode.FAILURE.getCode(), ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * 处理业务异常 - 由业务逻辑抛出的预期异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<R<?>> handleBusinessException(BusinessException ex, WebRequest request) {
        logger.warn("Business error: {}", ex.getMessage());
        R<?> response = R.fail(ex.getCode(), ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    /**
     * 处理系统异常 - 捕获所有未明确处理的异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<R<?>> handleGlobalException(Exception ex, WebRequest request) {
        logger.error("System error: {}", ex.getMessage(), ex);
        // 为了安全，不向客户端暴露详细的系统错误信息
        R<?> response = R.fail(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "Internal server error");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}