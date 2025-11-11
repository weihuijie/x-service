//package com.x.api.gateway.service.handler;
//
//import com.x.common.base.R;
//import com.x.common.base.ResultCode;
//import com.x.common.exception.BusinessException;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.core.annotation.Order;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
///**
// * 全局异常处理器 - 统一处理API网关中的所有异常
// * 使用@RestControllerAdvice注解捕获所有控制器中的异常
// *
// * @author whj
// */
//@Slf4j
//@RestControllerAdvice
//@Order(-2) // 设置高优先级
//public class GlobalExceptionHandler {
//
//    /**
//     * 处理参数验证异常 - 如缺少必填参数、参数格式错误等
//     */
//    @ExceptionHandler(IllegalArgumentException.class)
//    public Mono<ResponseEntity<R<?>>> handleIllegalArgumentException(IllegalArgumentException ex, ServerWebExchange exchange) {
//        log.warn("Bad request: {}", ex.getMessage());
//        R<?> response = R.fail(ResultCode.FAILURE.getCode(), ex.getMessage());
//        return Mono.just(new ResponseEntity<>(response, HttpStatus.BAD_REQUEST));
//    }
//
//    /**
//     * 处理业务异常 - 由业务逻辑抛出的预期异常
//     */
//    @ExceptionHandler(BusinessException.class)
//    public Mono<ResponseEntity<R<?>>> handleBusinessException(BusinessException ex, ServerWebExchange exchange) {
//        log.warn("Business error: {}", ex.getMessage());
//        R<?> response = R.fail(ex.getCode(), ex.getMessage());
//        return Mono.just(new ResponseEntity<>(response, HttpStatus.OK));
//    }
//
//    /**
//     * 处理系统异常 - 捕获所有未明确处理的异常
//     */
//    @ExceptionHandler(Exception.class)
//    public Mono<ResponseEntity<R<?>>> handleGlobalException(Exception ex, ServerWebExchange exchange) {
//        log.error("System error: {}", ex.getMessage(), ex);
//        // 为了安全，不向客户端暴露详细的系统错误信息
//        R<?> response = R.fail(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "Internal server error");
//        return Mono.just(new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR));
//    }
//}