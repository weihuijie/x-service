package com.x.auth.service.controller;

import com.x.auth.service.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    // 登录接口
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        // 在实际应用中，这里需要验证用户名和密码
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        
        // 简化的用户角色映射示例
        String role = getRoleByUsername(username);
        
        // 验证用户凭据（示例中直接通过）
        if (isValidUser(username, password)) {
            // 生成JWT token
            String token = jwtUtil.generateToken(username, role);
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("username", username);
            response.put("role", role);
            
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }
    
    // 验证token接口
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            
            try {
                String username = jwtUtil.extractUsername(token);
                String role = jwtUtil.extractRole(token);
                
                if (jwtUtil.validateToken(token, username)) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("valid", true);
                    response.put("username", username);
                    response.put("role", role);
                    
                    return ResponseEntity.ok(response);
                } else {
                    return ResponseEntity.status(401).body("Invalid token");
                }
            } catch (Exception e) {
                return ResponseEntity.status(401).body("Invalid token");
            }
        } else {
            return ResponseEntity.status(401).body("Missing or invalid token format");
        }
    }
    
    // 权限检查接口
    @PostMapping("/permission/check")
    public ResponseEntity<?> checkPermission(
            @RequestHeader("Authorization") String token,
            @RequestBody PermissionCheckRequest request) {
        
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            
            try {
                String username = jwtUtil.extractUsername(token);
                
                if (jwtUtil.validateToken(token, username)) {
                    boolean hasPermission = jwtUtil.hasPermission(token, request.getPermission());
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("hasPermission", hasPermission);
                    response.put("permission", request.getPermission());
                    
                    return ResponseEntity.ok(response);
                } else {
                    return ResponseEntity.status(401).body("Invalid token");
                }
            } catch (Exception e) {
                return ResponseEntity.status(401).body("Invalid token");
            }
        } else {
            return ResponseEntity.status(401).body("Missing or invalid token format");
        }
    }
    
    // 简化的用户验证方法
    private boolean isValidUser(String username, String password) {
        // 在实际应用中，这里需要查询数据库验证用户凭据
        // 示例中简单处理
        return username != null && !username.isEmpty() && 
               password != null && !password.isEmpty();
    }
    
    // 简化的角色获取方法
    private String getRoleByUsername(String username) {
        // 在实际应用中，这里需要查询数据库获取用户角色
        // 示例中简单映射
        switch (username) {
            case "admin":
                return "ADMIN";
            case "device_manager":
                return "DEVICE_MANAGER";
            case "analyst":
                return "DATA_ANALYST";
            default:
                return "USER";
        }
    }
    
    // 登录请求数据类
    public static class LoginRequest {
        private String username;
        private String password;
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
    }
    
    // 权限检查请求数据类
    public static class PermissionCheckRequest {
        private String permission;
        
        public String getPermission() {
            return permission;
        }
        
        public void setPermission(String permission) {
            this.permission = permission;
        }
    }
}