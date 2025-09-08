package com.x.demo.service.controller;

import com.x.demo.service.entity.Device;
import com.x.demo.service.entity.DeviceData;
import com.x.demo.service.service.AuthService;
import com.x.demo.service.service.DeviceService;
import com.x.demo.service.service.DataProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api")
public class DemoController {
    
    @Autowired
    private DeviceService deviceService;
    
    @Autowired
    private DataProcessingService dataProcessingService;
    
    @Autowired
    private AuthService authService;
    
    // 用户登录接口
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Map<String, Object> result = authService.login(request.getUsername(), request.getPassword());
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(401).body(result);
        }
    }
    
    // 验证Token接口
    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            Map<String, Object> result = authService.validateToken(token);
            return ResponseEntity.ok(result);
        } else {
            Map<String, Object> result = new HashMap<>();
            result.put("valid", false);
            result.put("message", "缺少或无效的Token格式");
            return ResponseEntity.status(401).body(result);
        }
    }
    
    // 创建设备接口
    @PostMapping("/devices")
    public ResponseEntity<?> createDevice(
            @RequestHeader("Authorization") String token,
            @RequestBody Device device) {
        
        if (!validateTokenAndPermission(token, "ADMIN")) {
            return ResponseEntity.status(403).body(createErrorResponse("权限不足"));
        }
        
        try {
            Device createdDevice = deviceService.saveDevice(device);
            return ResponseEntity.ok(createdDevice);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("创建设备失败: " + e.getMessage()));
        }
    }
    
    // 获取所有设备接口
    @GetMapping("/devices")
    public ResponseEntity<?> getAllDevices(@RequestHeader("Authorization") String token) {
        if (!validateTokenAndPermission(token, "USER")) {
            return ResponseEntity.status(403).body(createErrorResponse("权限不足"));
        }
        
        try {
            List<Device> devices = deviceService.getAllDevices();
            return ResponseEntity.ok(devices);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("获取设备列表失败: " + e.getMessage()));
        }
    }
    
    // 根据ID获取设备接口
    @GetMapping("/devices/{id}")
    public ResponseEntity<?> getDeviceById(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        
        if (!validateTokenAndPermission(token, "USER")) {
            return ResponseEntity.status(403).body(createErrorResponse("权限不足"));
        }
        
        try {
            Device device = deviceService.getDeviceById(id);
            if (device != null) {
                return ResponseEntity.ok(device);
            } else {
                return ResponseEntity.status(404).body(createErrorResponse("设备不存在"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("获取设备信息失败: " + e.getMessage()));
        }
    }
    
    // 更新设备状态接口
    @PutMapping("/devices/{id}/status")
    public ResponseEntity<?> updateDeviceStatus(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestBody UpdateStatusRequest request) {
        
        if (!validateTokenAndPermission(token, "ADMIN")) {
            return ResponseEntity.status(403).body(createErrorResponse("权限不足"));
        }
        
        try {
            Device updatedDevice = deviceService.updateDeviceStatus(id, request.getStatus());
            return ResponseEntity.ok(updatedDevice);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("更新设备状态失败: " + e.getMessage()));
        }
    }
    
    // 删除设备接口
    @DeleteMapping("/devices/{id}")
    public ResponseEntity<?> deleteDevice(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        
        if (!validateTokenAndPermission(token, "ADMIN")) {
            return ResponseEntity.status(403).body(createErrorResponse("权限不足"));
        }
        
        try {
            deviceService.deleteDevice(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("删除设备失败: " + e.getMessage()));
        }
    }
    
    // 发送设备数据到Kafka接口
    @PostMapping("/data/kafka")
    public ResponseEntity<?> sendDataToKafka(
            @RequestHeader("Authorization") String token,
            @RequestBody DeviceData deviceData) {
        
        if (!validateTokenAndPermission(token, "USER")) {
            return ResponseEntity.status(403).body(createErrorResponse("权限不足"));
        }
        
        try {
            dataProcessingService.processDataWithKafka(deviceData);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "数据已发送到Kafka");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("发送数据到Kafka失败: " + e.getMessage()));
        }
    }
    
    // 发送设备数据到RabbitMQ接口
    @PostMapping("/data/rabbitmq")
    public ResponseEntity<?> sendDataToRabbitMQ(
            @RequestHeader("Authorization") String token,
            @RequestBody DeviceData deviceData) {
        
        if (!validateTokenAndPermission(token, "USER")) {
            return ResponseEntity.status(403).body(createErrorResponse("权限不足"));
        }
        
        try {
            dataProcessingService.processDataWithRabbitMQ(deviceData);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "数据已发送到RabbitMQ");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("发送数据到RabbitMQ失败: " + e.getMessage()));
        }
    }
    
    // 存储设备数据到MinIO接口
    @PostMapping("/data/minio")
    public ResponseEntity<?> storeDataToMinIO(
            @RequestHeader("Authorization") String token,
            @RequestBody DeviceData deviceData) {
        
        if (!validateTokenAndPermission(token, "USER")) {
            return ResponseEntity.status(403).body(createErrorResponse("权限不足"));
        }
        
        try {
            String fileUrl = dataProcessingService.storeDataToMinIO(deviceData);
            if (fileUrl != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "数据已存储到MinIO");
                response.put("fileUrl", fileUrl);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(500).body(createErrorResponse("存储数据到MinIO失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("存储数据到MinIO失败: " + e.getMessage()));
        }
    }
    
    // 异步处理设备数据接口
    @PostMapping("/data/async")
    public ResponseEntity<?> processDeviceDataAsync(
            @RequestHeader("Authorization") String token,
            @RequestBody DeviceData deviceData) {
        
        if (!validateTokenAndPermission(token, "USER")) {
            return ResponseEntity.status(403).body(createErrorResponse("权限不足"));
        }
        
        try {
            CompletableFuture<String> future = dataProcessingService.processDeviceDataAsync(deviceData);
            String fileUrl = future.get(); // 等待异步操作完成
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "设备数据已异步处理");
            response.put("fileUrl", fileUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("异步处理设备数据失败: " + e.getMessage()));
        }
    }
    
    // 验证Token和权限的辅助方法
    private boolean validateTokenAndPermission(String token, String requiredRole) {
        if (token == null || !token.startsWith("Bearer ")) {
            return false;
        }
        
        token = token.substring(7);
        return authService.hasPermission(token, requiredRole);
    }
    
    // 创建错误响应的辅助方法
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", true);
        errorResponse.put("message", message);
        return errorResponse;
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
    
    // 更新设备状态请求数据类
    public static class UpdateStatusRequest {
        private String status;
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
    }
}