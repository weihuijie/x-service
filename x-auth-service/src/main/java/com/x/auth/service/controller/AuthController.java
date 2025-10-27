package com.x.auth.service.controller;

import com.x.auth.service.grpc.AuthServiceImpl;
import com.x.grpc.auth.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private AuthServiceImpl authService;
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody LoginRequestDTO request) {
        LoginRequest loginRequest = LoginRequest.newBuilder()
                .setUsername(request.username)
                .setPassword(request.password)
                .build();
        
        ResponseStreamObserver<LoginResponse> responseObserver = new ResponseStreamObserver<>();
        authService.login(loginRequest, responseObserver);
        LoginResponse response = responseObserver.getResponse();
        return new LoginResponseDTO(response);
    }
    
    /**
     * 验证Token
     */
    @PostMapping("/validate")
    public ValidateTokenResponseDTO validateToken(@RequestBody ValidateTokenRequestDTO request) {
        ValidateTokenRequest validateRequest = ValidateTokenRequest.newBuilder()
                .setToken(request.token)
                .build();
        
        ResponseStreamObserver<ValidateTokenResponse> responseObserver = new ResponseStreamObserver<>();
        authService.validateToken(validateRequest, responseObserver);
        ValidateTokenResponse response = responseObserver.getResponse();
        return new ValidateTokenResponseDTO(response);
    }
    
    /**
     * 检查权限
     */
    @PostMapping("/check-permission")
    public CheckPermissionResponseDTO checkPermission(@RequestBody CheckPermissionRequestDTO request) {
        CheckPermissionRequest checkRequest = CheckPermissionRequest.newBuilder()
                .setToken(request.token)
                .setPermission(request.permission)
                .build();
        
        ResponseStreamObserver<CheckPermissionResponse> responseObserver = new ResponseStreamObserver<>();
        authService.checkPermission(checkRequest, responseObserver);
        CheckPermissionResponse response = responseObserver.getResponse();
        return new CheckPermissionResponseDTO(response);
    }
    
    /**
     * 执行操作
     */
    @PostMapping("/execute")
    public ExecuteOperationResponseDTO executeOperation(@RequestBody Map<String, String> params) {
        ExecuteOperationRequest.Builder builder = ExecuteOperationRequest.newBuilder();
        builder.putAllParams(params);
        ExecuteOperationRequest request = builder.build();
        
        ResponseStreamObserver<ExecuteOperationResponse> responseObserver = new ResponseStreamObserver<>();
        // 注意：在原始的AuthServiceImpl中没有实现executeOperation方法，这里只是示例
        // authService.executeOperation(request, responseObserver);
        ExecuteOperationResponse response = responseObserver.getResponse();
        return new ExecuteOperationResponseDTO(response);
    }
    
    /**
     * 简单的StreamObserver实现，用于捕获gRPC响应
     */
    private static class ResponseStreamObserver<T> implements io.grpc.stub.StreamObserver<T> {
        private T response;
        private Throwable error;
        
        @Override
        public void onNext(T value) {
            this.response = value;
        }
        
        @Override
        public void onError(Throwable t) {
            this.error = t;
        }
        
        @Override
        public void onCompleted() {
            // 不需要特殊处理
        }
        
        public T getResponse() {
            if (error != null) {
                throw new RuntimeException(error);
            }
            return response;
        }
    }
    
    // DTO类定义
    @Setter
    @Getter
    public static class LoginRequestDTO {
        public String username;
        public String password;

    }
    @Setter
    @Getter
    public static class ValidateTokenRequestDTO {
        public String token;
    }
    @Setter
    @Getter
    public static class CheckPermissionRequestDTO {
        public String token;
        public String permission;
    }
    
    public static class LoginResponseDTO {
        public boolean success;
        public String message;
        public String token;
        public String username;
        public String role;
        public long expireTime;
        
        public LoginResponseDTO(LoginResponse response) {
            this.success = response.getSuccess();
            this.message = response.getMessage();
            this.token = response.getToken();
            this.username = response.getUsername();
            this.role = response.getRole();
            this.expireTime = response.getExpireTime();
        }
    }
    
    public static class ValidateTokenResponseDTO {
        public boolean valid;
        public String message;
        public String username;
        public String role;
        public long expireTime;
        
        public ValidateTokenResponseDTO(ValidateTokenResponse response) {
            this.valid = response.getValid();
            this.message = response.getMessage();
            this.username = response.getUsername();
            this.role = response.getRole();
            this.expireTime = response.getExpireTime();
        }
    }
    
    public static class CheckPermissionResponseDTO {
        public boolean hasPermission;
        public String message;
        
        public CheckPermissionResponseDTO(CheckPermissionResponse response) {
            this.hasPermission = response.getHasPermission();
            this.message = response.getMessage();
        }
    }
    
    public static class ExecuteOperationResponseDTO {
        public boolean success;
        public int code;
        public String message;
        public Map<String, String> data;
        
        public ExecuteOperationResponseDTO(ExecuteOperationResponse response) {
            this.success = response.getSuccess();
            this.code = response.getCode();
            this.message = response.getMessage();
            this.data = response.getDataMap();
        }
    }
}