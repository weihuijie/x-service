package com.x.api.gateway.service.client;

import com.x.grpc.auth.*;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Dubbo认证服务客户端 - 使用Dubbo调用认证服务的gRPC接口
 * 替代之前的Feign客户端，提供更高效的服务调用
 */
@Component
public class DubboAuthServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(DubboAuthServiceClient.class);

    // 完善DubboReference配置，添加timeout、retries、cluster和loadbalance等参数
    @DubboReference(
            version = "1.0.0",
            check = false,
            timeout = 3000,            // 超时时间3秒
            retries = 0,               // 不重试，避免重复操作
            cluster = "failfast",      // 快速失败
            loadbalance = "consistenthash" // 一致性哈希负载均衡
    )
    private DubboAuthServiceGrpc.IAuthService authService;

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 登录响应
     */
    public LoginResponse login(String username, String password) {
        return invokeWithErrorHandling(
                () -> {
                    logger.debug("Calling auth service login with username: {}", username);
                    LoginRequest request = LoginRequest.newBuilder()
                            .setUsername(username)
                            .setPassword(password)
                            .build();
                    return authService.login(request);
                },
                "login",
                () -> LoginResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Failed to call auth service: login")
                        .build()
        );
    }

    /**
     * 验证Token
     * @param token 用户token
     * @return 验证结果
     */
    public ValidateTokenResponse validateToken(String token) {
        return invokeWithErrorHandling(
                () -> {
                    logger.debug("Calling auth service validateToken");
                    ValidateTokenRequest request = ValidateTokenRequest.newBuilder()
                            .setToken(token)
                            .build();
                    return authService.validateToken(request);
                },
                "validateToken",
                () -> ValidateTokenResponse.newBuilder()
                        .setValid(false)
                        .setMessage("Failed to validate token")
                        .build()
        );
    }

    /**
     * 检查权限
     * @param token 用户token
     * @param permission 权限名称
     * @return 权限检查结果
     */
    public CheckPermissionResponse checkPermission(String token, String permission) {
        return invokeWithErrorHandling(
                () -> {
                    logger.debug("Calling auth service checkPermission for permission: {}", permission);
                    CheckPermissionRequest request = CheckPermissionRequest.newBuilder()
                            .setToken(token)
                            .setPermission(permission)
                            .build();
                    return authService.checkPermission(request);
                },
                "checkPermission",
                () -> CheckPermissionResponse.newBuilder()
                        .setHasPermission(false)
                        .setMessage("Failed to check permission")
                        .build()
        );
    }

    // 通用的Dubbo服务调用与错误处理
    private <T> T invokeWithErrorHandling(
            DubboServiceCall<T> call,
            String operation,
            FallbackProvider<T> fallbackProvider) {
        
        try {
            return call.call();
        } catch (Exception e) {
            logger.error("Failed to call auth service {}: {}", operation, e.getMessage(), e);
            return fallbackProvider.getFallback();
        }
    }

    // 函数式接口定义
    private interface DubboServiceCall<T> {
        T call();
    }

    private interface FallbackProvider<T> {
        T getFallback();
    }
}