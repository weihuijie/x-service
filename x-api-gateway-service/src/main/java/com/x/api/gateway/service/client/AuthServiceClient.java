package com.x.api.gateway.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 认证服务Feign客户端
 * 用于调用认证服务的API接口
 */
@FeignClient(name = "x-auth-service")
public interface AuthServiceClient {
    
    /**
     * 验证token有效性
     * @param tokenRequest 包含token的请求对象
     * @return 验证结果
     */
    @PostMapping("/auth/validate")
    ValidateTokenResponse validateToken(@RequestBody TokenRequest tokenRequest);
    
    /**
     * token请求对象
     */
    class TokenRequest {
        private String token;
        
        public TokenRequest(String token) {
            this.token = token;
        }
        
        public String getToken() {
            return token;
        }
        
        public void setToken(String token) {
            this.token = token;
        }
    }
    
    /**
     * 验证token响应对象
     */
    class ValidateTokenResponse {
        private boolean valid;
        private String userId;
        private String username;
        private String message;
        
        public boolean isValid() {
            return valid;
        }
        
        public void setValid(boolean valid) {
            this.valid = valid;
        }
        
        public String getUserId() {
            return userId;
        }
        
        public void setUserId(String userId) {
            this.userId = userId;
        }
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
}