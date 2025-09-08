package com.x.demo.service.service;

import com.x.demo.service.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    // 用户登录并生成JWT token
    public Map<String, Object> login(String username, String password) {
        // 简化处理，实际应该查询数据库验证用户凭据
        if (isValidUser(username, password)) {
            // 生成JWT token
            String token = jwtUtil.generateToken(username, getUserRole(username));
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("token", token);
            response.put("username", username);
            response.put("role", getUserRole(username));
            
            return response;
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "用户名或密码错误");
            return response;
        }
    }
    
    // 验证JWT token
    public Map<String, Object> validateToken(String token) {
        try {
            String username = jwtUtil.extractUsername(token);
            String role = jwtUtil.extractRole(token);
            
            if (jwtUtil.validateToken(token, username)) {
                Map<String, Object> response = new HashMap<>();
                response.put("valid", true);
                response.put("username", username);
                response.put("role", role);
                return response;
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("valid", false);
                response.put("message", "Token已过期");
                return response;
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("message", "Token无效");
            return response;
        }
    }
    
    // 检查用户是否有权限访问特定资源
    public boolean hasPermission(String token, String requiredRole) {
        try {
            String username = jwtUtil.extractUsername(token);
            if (jwtUtil.validateToken(token, username)) {
                String userRole = jwtUtil.extractRole(token);
                return userRole != null && userRole.equals(requiredRole);
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    // 简化的用户验证方法
    private boolean isValidUser(String username, String password) {
        // 模拟用户验证，实际应该查询数据库
        return "admin".equals(username) && "password".equals(password);
    }
    
    // 获取用户角色
    private String getUserRole(String username) {
        // 模拟获取用户角色，实际应该查询数据库
        if ("admin".equals(username)) {
            return "ADMIN";
        }
        return "USER";
    }
}