package com.x.auth.service.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {
    
    private String secret = "x_service_secret_key_which_should_be_at_least_256_bits_long_for_HS256_algorithm";
    
    private Long expiration = 3600000L;
    
    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return createToken(claims, username);
    }
    
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }
    
    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }
    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }
    
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }
    
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    // 角色检查方法
    public Boolean hasRole(String token, String requiredRole) {
        final String role = extractRole(token);
        return role != null && role.equals(requiredRole);
    }
    
    // 权限检查方法
    public Boolean hasPermission(String token, String requiredPermission) {
        // 在实际应用中，这里会根据用户角色检查权限
        final String role = extractRole(token);
        if (role == null) return false;
        
        // 简化的权限映射示例
        switch (role) {
            case "ADMIN":
                return true; // 管理员拥有所有权限
            case "DEVICE_MANAGER":
                return "device_management".equals(requiredPermission);
            case "DATA_ANALYST":
                return "data_analysis".equals(requiredPermission);
            case "USER":
                return "read_only".equals(requiredPermission);
            default:
                return false;
        }
    }
}