package com.x.common.handler;

import com.x.common.utils.http.HttpEnhancedUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring 全局异常处理器
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // 捕获 HttpRetryException
    @ExceptionHandler(HttpEnhancedUtils.HttpRetryException.class)
    @ResponseBody
    public Map<String, Object> handleHttpRetryException(HttpEnhancedUtils.HttpRetryException e, HttpServletRequest request) {
        log.error("HTTP请求失败：{}，URL：{}，状态码：{}",
                e.getMessage(), e.getUrl(), e.getStatusCode(), e);

        Map<String, Object> result = new HashMap<>();
        result.put("code", e.getStatusCode());
        result.put("msg", "请求第三方接口失败：" + e.getMessage());
        result.put("data", null);
        return result;
    }

    // 捕获其他运行时异常
    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public Map<String, Object> handleRuntimeException(RuntimeException e) {
        log.error("系统异常：{}", e.getMessage(), e);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 500);
        result.put("msg", "系统异常");
        result.put("data", null);
        return result;
    }
}