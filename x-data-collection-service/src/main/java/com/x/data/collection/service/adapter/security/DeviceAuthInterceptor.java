package com.x.data.collection.service.adapter.security;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 设备认证拦截器
 * 用于拦截HTTP请求并进行设备身份验证
 *
 * @author whj
 */
@Slf4j
@Component
public class DeviceAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private DeviceAuthService deviceAuthService;


    public boolean preHandle(@NotNull HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String deviceId = request.getHeader("Device-ID");
        String token = request.getHeader("Authorization");

        // 如果是设备数据接口，则需要进行鉴权
        if (request.getRequestURI().startsWith("/api/device/data")) {
            // 验证设备ID和Token是否存在
            if (deviceId == null || deviceId.isBlank() || token == null || token.isBlank()) {
                log.warn("设备ID或Token为空: deviceId={}, token={}", deviceId, token);
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "设备ID或Token不能为空");
                return false;
            }

            // 验证设备身份
            if (!deviceAuthService.authenticateDevice(deviceId, token)) {
                log.warn("设备身份验证失败: deviceId={}, token={}", deviceId, token);
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "设备身份验证失败");
                return false;
            }

            // 验证设备权限
            if (!deviceAuthService.hasPermission(deviceId)) {
                log.warn("设备没有推送数据的权限: deviceId={}", deviceId);
                sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "设备没有推送数据的权限");
                return false;
            }
        }

        return true;
    }

    /**
     * 统一错误响应处理，避免重复代码并设置正确的响应头
     */
    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8"); // 设置JSON格式和编码
        response.getWriter().write(String.format("{\"code\":%d,\"error\":\"%s\"}", status, message));
    }
}