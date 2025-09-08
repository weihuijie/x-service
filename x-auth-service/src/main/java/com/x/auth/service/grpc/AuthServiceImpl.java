package com.x.auth.service.grpc;

import com.x.grpc.auth.*;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@GrpcService
public class AuthServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {
    
    // 模拟用户数据库
    private static final Map<String, String> USER_PASSWORDS = new HashMap<>();
    private static final Map<String, String> USER_ROLES = new HashMap<>();
    
    static {
        USER_PASSWORDS.put("admin", "admin123");
        USER_ROLES.put("admin", "ADMIN");
        
        USER_PASSWORDS.put("user", "user123");
        USER_ROLES.put("user", "USER");
    }
    
    @Override
    public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
        try {
            String username = request.getUsername();
            String password = request.getPassword();
            
            // 验证用户凭据
            if (USER_PASSWORDS.containsKey(username) && USER_PASSWORDS.get(username).equals(password)) {
                // 生成模拟token
                String token = UUID.randomUUID().toString();
                String role = USER_ROLES.get(username);
                long expireTime = System.currentTimeMillis() + 3600000; // 1小时后过期
                
                LoginResponse response = LoginResponse.newBuilder()
                        .setSuccess(true)
                        .setMessage("登录成功")
                        .setToken(token)
                        .setUsername(username)
                        .setRole(role)
                        .setExpireTime(expireTime)
                        .build();
                
                responseObserver.onNext(response);
            } else {
                LoginResponse response = LoginResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("用户名或密码错误")
                        .build();
                
                responseObserver.onNext(response);
            }
            
            responseObserver.onCompleted();
        } catch (Exception e) {
            LoginResponse response = LoginResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("登录失败: " + e.getMessage())
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
    
    @Override
    public void validateToken(ValidateTokenRequest request, StreamObserver<ValidateTokenResponse> responseObserver) {
        try {
            String token = request.getToken();
            
            // 模拟token验证（实际项目中应该验证JWT token）
            if (token != null && !token.isEmpty()) {
                // 模拟用户信息
                String username = "admin";
                String role = "ADMIN";
                long expireTime = System.currentTimeMillis() + 3600000;
                
                ValidateTokenResponse response = ValidateTokenResponse.newBuilder()
                        .setValid(true)
                        .setMessage("Token有效")
                        .setUsername(username)
                        .setRole(role)
                        .setExpireTime(expireTime)
                        .build();
                
                responseObserver.onNext(response);
            } else {
                ValidateTokenResponse response = ValidateTokenResponse.newBuilder()
                        .setValid(false)
                        .setMessage("Token无效")
                        .build();
                
                responseObserver.onNext(response);
            }
            
            responseObserver.onCompleted();
        } catch (Exception e) {
            ValidateTokenResponse response = ValidateTokenResponse.newBuilder()
                    .setValid(false)
                    .setMessage("Token验证失败: " + e.getMessage())
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
    
    @Override
    public void checkPermission(CheckPermissionRequest request, StreamObserver<CheckPermissionResponse> responseObserver) {
        try {
            String token = request.getToken();
            String permission = request.getPermission();
            
            // 模拟权限检查（实际项目中应该根据用户角色检查权限）
            if (token != null && !token.isEmpty() && permission != null && !permission.isEmpty()) {
                // 模拟管理员拥有所有权限
                boolean hasPermission = true;
                
                CheckPermissionResponse response = CheckPermissionResponse.newBuilder()
                        .setHasPermission(hasPermission)
                        .setMessage(hasPermission ? "权限检查通过" : "权限不足")
                        .build();
                
                responseObserver.onNext(response);
            } else {
                CheckPermissionResponse response = CheckPermissionResponse.newBuilder()
                        .setHasPermission(false)
                        .setMessage("权限检查失败")
                        .build();
                
                responseObserver.onNext(response);
            }
            
            responseObserver.onCompleted();
        } catch (Exception e) {
            CheckPermissionResponse response = CheckPermissionResponse.newBuilder()
                    .setHasPermission(false)
                    .setMessage("权限检查异常: " + e.getMessage())
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}