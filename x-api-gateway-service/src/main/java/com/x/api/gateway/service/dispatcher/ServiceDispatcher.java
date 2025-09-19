package com.x.api.gateway.service.dispatcher;

import com.x.api.gateway.service.client.DubboAuthServiceClient;
import com.x.api.gateway.service.client.DubboDeviceServiceClient;
import com.x.common.base.R;
import com.x.common.base.ResultCode;
import com.x.grpc.auth.LoginResponse;
import com.x.grpc.auth.ValidateTokenResponse;
import com.x.grpc.device.CreateDeviceResponse;
import com.x.grpc.device.Device;
import com.x.grpc.device.GetDeviceResponse;
import com.x.grpc.device.ListDevicesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 服务分发器 - 在请求拦截后根据路径和请求参数将请求分发到相应的Dubbo服务
 * 负责处理不同服务的调用逻辑，实现请求的路由分发
 */
@Component
public class ServiceDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(ServiceDispatcher.class);
    
    // 自定义线程池，避免使用默认的ForkJoinPool
    private static final ExecutorService serviceExecutor = Executors.newFixedThreadPool(
            Math.max(4, Runtime.getRuntime().availableProcessors()),
            r -> {
                Thread t = new Thread(r, "service-dispatcher-");
                t.setDaemon(true);
                return t;
            }
    );

    @Autowired
    private DubboAuthServiceClient authServiceClient;

    @Autowired
    private DubboDeviceServiceClient deviceServiceClient;

    /**
     * 处理认证相关请求
     * @param operation 操作类型(login/validateToken等)
     * @param params 请求参数
     * @return 处理结果
     */
    public CompletableFuture<R<Map<String, Object>>> dispatchAuthRequest(String operation, Map<String, Object> params) {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("Dispatching auth request: {}", operation);
            
            try {
                switch (operation) {
                    case "login":
                        return handleLogin(params);
                    case "validateToken":
                        return handleValidateToken(params);
                    default:
                        return R.fail(ResultCode.FAILURE, "Unsupported auth operation: " + operation);
                }
            } catch (Exception e) {
                return handleException("auth", operation, e);
            }
        }, serviceExecutor);
    }

    /**
     * 处理设备相关请求
     * @param operation 操作类型(createDevice/getDevice等)
     * @param params 请求参数
     * @return 处理结果
     */
    public CompletableFuture<R<Map<String, Object>>> dispatchDeviceRequest(String operation, Map<String, Object> params) {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("Dispatching device request: {}", operation);
            
            try {
                switch (operation) {
                    case "createDevice":
                        return handleCreateDevice(params);
                    case "getDevice":
                        return handleGetDevice(params);
                    case "listDevices":
                        return handleListDevices(params);
                    default:
                        return R.fail(ResultCode.FAILURE, "Unsupported device operation: " + operation);
                }
            } catch (Exception e) {
                return handleException("device", operation, e);
            }
        }, serviceExecutor);
    }

    // 提取处理方法，提高代码可读性和可维护性
    private R<Map<String, Object>> handleLogin(Map<String, Object> params) {
        validateRequiredParams(params, "username", "password");
        
        String username = (String) params.get("username");
        String password = (String) params.get("password");
        LoginResponse loginResponse = authServiceClient.login(username, password);
        
        if (loginResponse.getSuccess()) {
            Map<String, Object> data = new HashMap<>();
            data.put("token", loginResponse.getToken());
            data.put("username", loginResponse.getUsername());
            data.put("role", loginResponse.getRole());
            return R.success(ResultCode.SUCCESS, loginResponse.getMessage()).data(data);
        } else {
            return R.fail(ResultCode.FAILURE, loginResponse.getMessage());
        }
    }

    private R<Map<String, Object>> handleValidateToken(Map<String, Object> params) {
        validateRequiredParams(params, "token");
        
        String token = (String) params.get("token");
        ValidateTokenResponse validateResponse = authServiceClient.validateToken(token);
        
        if (validateResponse.getValid()) {
            Map<String, Object> data = new HashMap<>();
            data.put("valid", validateResponse.getValid());
            data.put("username", validateResponse.getUsername());
            data.put("role", validateResponse.getRole());
            return R.success(ResultCode.SUCCESS, validateResponse.getMessage()).data(data);
        } else {
            return R.fail(ResultCode.FAILURE, validateResponse.getMessage());
        }
    }

    private R<Map<String, Object>> handleCreateDevice(Map<String, Object> params) {
        validateRequiredParams(params, "name", "model", "status", "location");
        
        String name = (String) params.get("name");
        String model = (String) params.get("model");
        String status = (String) params.get("status");
        String location = (String) params.get("location");
        
        CreateDeviceResponse createResponse = deviceServiceClient.createDevice(name, model, status, location);
        
        if (createResponse.getSuccess() && createResponse.hasDevice()) {
            Map<String, Object> data = new HashMap<>();
            data.put("device", convertDeviceToMap(createResponse.getDevice()));
            return R.success(ResultCode.SUCCESS, createResponse.getMessage()).data(data);
        } else {
            return R.fail(ResultCode.FAILURE, createResponse.getMessage());
        }
    }

    private R<Map<String, Object>> handleGetDevice(Map<String, Object> params) {
        validateRequiredParams(params, "id");
        
        Long id = safeParseLong(params.get("id"));
        GetDeviceResponse getResponse = deviceServiceClient.getDevice(id);
        
        if (getResponse.getSuccess() && getResponse.hasDevice()) {
            Map<String, Object> data = new HashMap<>();
            data.put("device", convertDeviceToMap(getResponse.getDevice()));
            return R.success(ResultCode.SUCCESS, getResponse.getMessage()).data(data);
        } else {
            return R.fail(ResultCode.FAILURE, getResponse.getMessage());
        }
    }

    private R<Map<String, Object>> handleListDevices(Map<String, Object> params) {
        int page = safeParseInt(params.getOrDefault("page", 1));
        int size = safeParseInt(params.getOrDefault("size", 10));
        String deviceStatus = params.containsKey("status") ? (String) params.get("status") : null;
        
        ListDevicesResponse listResponse = deviceServiceClient.listDevices(page, size, deviceStatus);
        
        if (listResponse.getSuccess()) {
            Map<String, Object> data = new HashMap<>();
            data.put("devices", listResponse.getDevicesList().stream()
                    .map(this::convertDeviceToMap)
                    .toArray());
            return R.success(ResultCode.SUCCESS, listResponse.getMessage()).data(data);
        } else {
            return R.fail(ResultCode.FAILURE, listResponse.getMessage());
        }
    }

    // 通用异常处理
    private R<Map<String, Object>> handleException(String serviceType, String operation, Exception e) {
        logger.error("Error dispatching {} request [{}]: {}", serviceType, operation, e.getMessage(), e);
        
        if (e instanceof IllegalArgumentException) {
            return R.fail(ResultCode.PARAM_VALID_ERROR, e.getMessage());
        } else {
            return R.fail(ResultCode.INTERNAL_SERVER_ERROR, "Error processing request: " + e.getMessage());
        }
    }

    // 参数验证
    private void validateRequiredParams(Map<String, Object> params, String... requiredFields) {
        for (String field : requiredFields) {
            if (!params.containsKey(field) || params.get(field) == null || 
                (params.get(field) instanceof String && ((String) params.get(field)).trim().isEmpty())) {
                throw new IllegalArgumentException("Missing required parameter: " + field);
            }
        }
    }

    // 安全的类型转换
    private Long safeParseLong(Object value) {
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException | NullPointerException e) {
            throw new IllegalArgumentException("Invalid number format: " + value);
        }
    }

    private Integer safeParseInt(Object value) {
        try {
            return Integer.valueOf(value.toString());
        } catch (NumberFormatException | NullPointerException e) {
            throw new IllegalArgumentException("Invalid integer format: " + value);
        }
    }

    /**
     * 将Device对象转换为Map，便于JSON序列化
     */
    private Map<String, Object> convertDeviceToMap(Device device) {
        Map<String, Object> deviceMap = new HashMap<>();
        deviceMap.put("id", device.getId());
        deviceMap.put("name", device.getName());
        deviceMap.put("model", device.getModel());
        deviceMap.put("status", device.getStatus());
        deviceMap.put("location", device.getLocation());
        deviceMap.put("createTime", device.getCreateTime());
        deviceMap.put("updateTime", device.getUpdateTime());
        return deviceMap;
    }
}