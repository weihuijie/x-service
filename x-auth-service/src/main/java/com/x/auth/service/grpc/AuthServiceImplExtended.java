package com.x.auth.service.grpc;

import com.x.grpc.auth.*;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import com.x.auth.service.util.JwtUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@GrpcService
public class AuthServiceImplExtended extends AuthServiceGrpc.AuthServiceImplBase {
    
    private final JwtUtil jwtUtil = new JwtUtil();
    
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
    
    @Override
    public void executeOperation(ExecuteOperationRequest request, StreamObserver<ExecuteOperationResponse> responseObserver) {
        try {
            String operation = request.getOperation();
            Map<String, String> params = request.getParamsMap();
            
            Map<String, String> resultData = new HashMap<>();
            boolean success = true;
            String message = "操作成功";
            int code = 200;
            
            switch (operation.toLowerCase()) {
                case "login":
                    success = handleLoginOperation(params, resultData);
                    message = success ? "登录成功" : "登录失败";
                    break;
                case "validatetoken":
                    success = handleValidateTokenOperation(params, resultData);
                    message = success ? "Token验证成功" : "Token验证失败";
                    break;
                default:
                    success = false;
                    message = "不支持的操作: " + operation;
                    code = 400;
                    break;
            }
            
            ExecuteOperationResponse response = ExecuteOperationResponse.newBuilder()
                    .setSuccess(success)
                    .setCode(code)
                    .setMessage(message)
                    .putAllData(resultData)
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            ExecuteOperationResponse response = ExecuteOperationResponse.newBuilder()
                    .setSuccess(false)
                    .setCode(500)
                    .setMessage("操作执行失败: " + e.getMessage())
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
    
    private boolean handleLoginOperation(Map<String, String> params, Map<String, String> resultData) {
        String username = params.get("username");
        String password = params.get("password");
        
        if (username == null || password == null) {
            resultData.put("error", "用户名和密码不能为空");
            return false;
        }
        
        // 验证用户凭据
        if (USER_PASSWORDS.containsKey(username) && USER_PASSWORDS.get(username).equals(password)) {
            // 生成模拟token
            String token = UUID.randomUUID().toString();
            String role = USER_ROLES.get(username);
            
            resultData.put("token", token);
            resultData.put("username", username);
            resultData.put("role", role);
            return true;
        } else {
            resultData.put("error", "用户名或密码错误");
            return false;
        }
    }
    
    private boolean handleValidateTokenOperation(Map<String, String> params, Map<String, String> resultData) {
        String token = params.get("token");
        
        if (token == null) {
            resultData.put("error", "Token不能为空");
            return false;
        }
        
        // 模拟token验证
        if (!token.isEmpty()) {
            // 模拟用户信息
            String username = "admin";
            String role = "ADMIN";
            
            resultData.put("valid", "true");
            resultData.put("username", username);
            resultData.put("role", role);
            return true;
        } else {
            resultData.put("valid", "false");
            resultData.put("error", "Token无效");
            return false;
        }
    }
}