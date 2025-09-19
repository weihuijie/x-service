# X-Service 项目优化方案

本文档针对X-Service物联网设备监控与管理系统提出全面的优化建议，包括代码优化、架构优化和最佳实践。

## 1. 组件工具类问题及优化

### 1.1 文档与代码一致性问题

**问题描述**：
- `IoTDBComponentUtil` 类在 `COMPONENT_UTILS.md` 文档中提及，但实际代码中不存在。
- `COMPONENT_ISSUES.md` 中描述的 `XxlJobComponentUtil` 类的 `getEnv` 方法问题与实际代码不符，实际代码已经正确实现。

**优化建议**：
1. **更新文档**：修正 `COMPONENT_UTILS.md` 和 `COMPONENT_ISSUES.md` 文档，确保与实际代码一致。
2. **创建缺失工具类**：如果确实需要 `IoTDBComponentUtil`，参考项目中 `IotDBServiceImpl` 的实现创建该工具类。

### 1.2 FlinkComponentUtil 类优化

**问题描述**：
- 存在重复的方法注释（对数据流进行键控分组的方法）。

**优化建议**：

```java
// 修改前
/**
 * 对数据流进行键控分组
 * @param dataStream 数据流
 * @param keySelector 键选择器
 * @param <T> 数据类型
 * @param <K> 键类型
 * @return 键控流
 */

/**
 * 执行流处理任务
 * @param env 流执行环境
 * @param jobName 作业名称
 * @throws Exception 异常
 */
public void executeJob(StreamExecutionEnvironment env, String jobName) throws Exception {
    env.execute(jobName);
}

/**
 * 对数据流进行键控分组
 * @param dataStream 数据流
 * @param keySelector 键选择器
 * @param <T> 数据类型
 * @param <K> 键类型
 * @return 键控流
 */
public <T, K> KeyedStream<T, K> keyByDataStream(DataStream<T> dataStream, KeySelector<T, K> keySelector) {
    return dataStream.keyBy(keySelector);
}

// 修改后
/**
 * 执行流处理任务
 * @param env 流执行环境
 * @param jobName 作业名称
 * @throws Exception 异常
 */
public void executeJob(StreamExecutionEnvironment env, String jobName) throws Exception {
    env.execute(jobName);
}

/**
 * 对数据流进行键控分组
 * @param dataStream 数据流
 * @param keySelector 键选择器
 * @param <T> 数据类型
 * @param <K> 键类型
 * @return 键控流
 */
public <T, K> KeyedStream<T, K> keyByDataStream(DataStream<T> dataStream, KeySelector<T, K> keySelector) {
    return dataStream.keyBy(keySelector);
}
```

## 2. 服务分发机制优化

### 2.1 ServiceDispatcher 类优化

**问题描述**：
- 使用 `CompletableFuture.supplyAsync()` 但未指定自定义线程池，可能导致使用默认的 ForkJoinPool，影响性能。
- 参数类型转换不够健壮。
- 异常处理逻辑重复。

**优化建议**：

```java
package com.x.api.gateway.service.dispatcher;

import com.x.api.gateway.service.client.DubboAuthServiceClient;
import com.x.api.gateway.service.client.DubboDeviceServiceClient;
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
     */
    public CompletableFuture<Map<String, Object>> dispatchAuthRequest(String operation, Map<String, Object> params) {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("Dispatching auth request: {}", operation);
            Map<String, Object> result = new HashMap<>();
            
            try {
                switch (operation) {
                    case "login":
                        handleLogin(params, result);
                        break;
                    case "validateToken":
                        handleValidateToken(params, result);
                        break;
                    default:
                        result.put("success", false);
                        result.put("message", "Unsupported auth operation: " + operation);
                        break;
                }
            } catch (Exception e) {
                handleException("auth", operation, e, result);
            }
            
            return result;
        }, serviceExecutor);
    }

    /**
     * 处理设备相关请求
     */
    public CompletableFuture<Map<String, Object>> dispatchDeviceRequest(String operation, Map<String, Object> params) {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("Dispatching device request: {}", operation);
            Map<String, Object> result = new HashMap<>();
            
            try {
                switch (operation) {
                    case "createDevice":
                        handleCreateDevice(params, result);
                        break;
                    case "getDevice":
                        handleGetDevice(params, result);
                        break;
                    case "listDevices":
                        handleListDevices(params, result);
                        break;
                    default:
                        result.put("success", false);
                        result.put("message", "Unsupported device operation: " + operation);
                        break;
                }
            } catch (Exception e) {
                handleException("device", operation, e, result);
            }
            
            return result;
        }, serviceExecutor);
    }

    // 提取处理方法，提高代码可读性和可维护性
    private void handleLogin(Map<String, Object> params, Map<String, Object> result) {
        validateRequiredParams(params, "username", "password");
        
        String username = (String) params.get("username");
        String password = (String) params.get("password");
        LoginResponse loginResponse = authServiceClient.login(username, password);
        
        result.put("success", loginResponse.getSuccess());
        result.put("message", loginResponse.getMessage());
        if (loginResponse.getSuccess()) {
            result.put("token", loginResponse.getToken());
            result.put("username", loginResponse.getUsername());
            result.put("role", loginResponse.getRole());
        }
    }

    private void handleValidateToken(Map<String, Object> params, Map<String, Object> result) {
        validateRequiredParams(params, "token");
        
        String token = (String) params.get("token");
        ValidateTokenResponse validateResponse = authServiceClient.validateToken(token);
        
        result.put("valid", validateResponse.getValid());
        result.put("message", validateResponse.getMessage());
        if (validateResponse.getValid()) {
            result.put("username", validateResponse.getUsername());
            result.put("role", validateResponse.getRole());
        }
    }

    private void handleCreateDevice(Map<String, Object> params, Map<String, Object> result) {
        validateRequiredParams(params, "name", "model", "status", "location");
        
        String name = (String) params.get("name");
        String model = (String) params.get("model");
        String status = (String) params.get("status");
        String location = (String) params.get("location");
        
        CreateDeviceResponse createResponse = deviceServiceClient.createDevice(name, model, status, location);
        result.put("success", createResponse.getSuccess());
        result.put("message", createResponse.getMessage());
        if (createResponse.getSuccess() && createResponse.hasDevice()) {
            result.put("device", convertDeviceToMap(createResponse.getDevice()));
        }
    }

    private void handleGetDevice(Map<String, Object> params, Map<String, Object> result) {
        validateRequiredParams(params, "id");
        
        Long id = safeParseLong(params.get("id"));
        GetDeviceResponse getResponse = deviceServiceClient.getDevice(id);
        
        result.put("success", getResponse.getSuccess());
        result.put("message", getResponse.getMessage());
        if (getResponse.getSuccess() && getResponse.hasDevice()) {
            result.put("device", convertDeviceToMap(getResponse.getDevice()));
        }
    }

    private void handleListDevices(Map<String, Object> params, Map<String, Object> result) {
        int page = safeParseInt(params.getOrDefault("page", 1));
        int size = safeParseInt(params.getOrDefault("size", 10));
        String deviceStatus = params.containsKey("status") ? (String) params.get("status") : null;
        
        ListDevicesResponse listResponse = deviceServiceClient.listDevices(page, size, deviceStatus);
        result.put("success", listResponse.getSuccess());
        result.put("message", listResponse.getMessage());
        if (listResponse.getSuccess()) {
            result.put("devices", listResponse.getDevicesList().stream()
                    .map(this::convertDeviceToMap)
                    .toArray());
        }
    }

    // 通用异常处理
    private void handleException(String serviceType, String operation, Exception e, Map<String, Object> result) {
        logger.error("Error dispatching {} request [{}]: {}", serviceType, operation, e.getMessage(), e);
        result.put("success", false);
        result.put("message", "Error processing request: " + e.getMessage());
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
```

### 2.2 Dubbo服务客户端优化

**问题描述**：
- `DubboReference` 注解配置不够完善，缺少 timeout、retries 等重要配置。
- 异常处理逻辑重复。
- 没有实现服务降级或熔断机制。

**优化建议**：

```java
package com.x.api.gateway.service.client;

import com.x.grpc.auth.AuthServiceProto;
import com.x.grpc.auth.LoginRequest;
import com.x.grpc.auth.LoginResponse;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.rpc.cluster.RpcStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class DubboAuthServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(DubboAuthServiceClient.class);

    // 完善DubboReference配置
    @DubboReference(
            version = "1.0.0",
            check = false,
            timeout = 3000,            // 超时时间3秒
            retries = 0,               // 不重试，避免重复操作
            cluster = "failfast",      // 快速失败
            loadbalance = "consistenthash" // 一致性哈希负载均衡
    )
    private AuthServiceProto.AuthService authService;

    /**
     * 用户登录
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

    // 其他方法类似优化...

    // 通用的Dubbo服务调用与错误处理
    private <T> T invokeWithErrorHandling(
            DubboServiceCall<T> call,
            String operation,
            FallbackProvider<T> fallbackProvider) {
        
        // 简单的服务熔断判断 - 实际项目中可以集成Sentinel或Resilience4j
        String serviceKey = "AuthService." + operation;
        RpcStatus status = RpcStatus.getStatus(authService, serviceKey);
        
        // 如果失败率超过50%，则触发熔断
        if (status.getActive() > 10 && status.getFailed() * 2 > status.getTotal()) {
            logger.warn("Circuit breaker triggered for {}", serviceKey);
            return fallbackProvider.getFallback();
        }
        
        try {
            return call.call();
        } catch (Exception e) {
            logger.error("Failed to call auth service {}: {}", operation, e.getMessage(), e);
            try {
                // 记录失败状态
                RpcStatus.recordError(1, TimeUnit.MILLISECONDS, status);
            } catch (Exception ex) {
                // 忽略记录状态时的异常
            }
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
```

## 3. 整体架构优化

### 3.1 统一响应封装

**问题描述**：
- 各服务返回的响应格式不统一，缺少标准化的响应封装。

**优化建议**：
创建统一的响应结果类，并在x-common模块中提供：

```java
package com.x.common.base;

import java.io.Serializable;

public class R<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int code;
    private String message;
    private T data;
    private long timestamp;
    
    private R() {
        this.timestamp = System.currentTimeMillis();
    }
    
    private R(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    // 成功响应
    public static <T> R<T> success(T data) {
        return new R<>(200, "Success", data);
    }
    
    // 失败响应
    public static <T> R<T> fail(String message) {
        return new R<>(400, message, null);
    }
    
    // 失败响应带状态码
    public static <T> R<T> fail(int code, String message) {
        return new R<>(code, message, null);
    }
    
    // Getters and setters
    public int getCode() {
        return code;
    }
    
    public void setCode(int code) {
        this.code = code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
}
```

### 3.2 全局异常处理

**问题描述**：
- 各服务的异常处理逻辑分散且重复。

**优化建议**：
在API网关和各服务中添加全局异常处理：

```java
package com.x.api.gateway.service.handler;

import com.x.common.base.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    // 处理参数验证异常
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<R<?>> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        logger.warn("Bad request: {}", ex.getMessage());
        R<?> response = R.fail(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    // 处理业务异常
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<R<?>> handleBusinessException(BusinessException ex, WebRequest request) {
        logger.warn("Business error: {}", ex.getMessage());
        R<?> response = R.fail(ex.getCode(), ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    // 处理系统异常
    @ExceptionHandler(Exception.class)
    public ResponseEntity<R<?>> handleGlobalException(Exception ex, WebRequest request) {
        logger.error("System error: {}", ex.getMessage(), ex);
        R<?> response = R.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```

### 3.3 服务网关优化

**问题描述**：
- API网关缺少请求限流、熔断和重试机制。
- 认证逻辑与业务逻辑耦合。

**优化建议**：
1. **集成Sentinel进行流量控制**：

```java
// 在网关配置类中添加
@Configuration
public class GatewayConfiguration {
    
    @Bean
    public SentinelGatewayFilter sentinelGatewayFilter() {
        return new SentinelGatewayFilter();
    }
    
    @Bean
    @Order(-1)
    public GlobalFilter sentinelGatewayBlockExceptionHandler() {
        return new SentinelGatewayBlockExceptionHandler();
    }
    
    @PostConstruct
    public void doInit() {
        // 配置网关限流规则
        setGatewayFlowRule();
        // 配置熔断降级规则
        setDegradeRule();
    }
    
    private void setGatewayFlowRule() {
        Set<GatewayFlowRule> rules = new HashSet<>();
        // 为设备服务配置限流
        rules.add(new GatewayFlowRule("device-service")
                .setCount(100)  // 每秒100个请求
                .setIntervalSec(1)
                .setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT));
        // 为认证服务配置限流
        rules.add(new GatewayFlowRule("auth-service")
                .setCount(50)   // 每秒50个请求
                .setIntervalSec(1));
        GatewayRuleManager.loadRules(rules);
    }
    
    private void setDegradeRule() {
        List<DegradeRule> rules = new ArrayList<>();
        // 配置服务熔断规则
        rules.add(new DegradeRule("device-service")
                .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO)
                .setCount(0.5)  // 异常比率超过50%时熔断
                .setTimeWindow(10));  // 熔断10秒
        DegradeRuleManager.loadRules(rules);
    }
}
```

2. **分离认证逻辑**：

```java
// 创建专门的认证配置类
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf().disable()
                .authorizeExchange()
                .pathMatchers("/auth/login", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyExchange().authenticated()
                .and()
                .addFilterAt(new JwtAuthenticationFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

## 4. 性能优化建议

### 4.1 数据库优化

**问题描述**：
- 未明确看到数据库索引优化和连接池配置。

**优化建议**：
1. **添加合适的索引**：为经常用于查询条件、排序和连接的字段添加索引。
2. **配置数据库连接池**：

```yaml
# application.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

### 4.2 缓存优化

**问题描述**：
- 系统中使用了Redis，但未看到统一的缓存策略。

**优化建议**：
1. **使用Spring Cache抽象**：

```java
@Configuration
@EnableCaching
public class CacheConfiguration {
    
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
        
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(config)
                .withCacheConfiguration("deviceCache", 
                        RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(1)))
                .withCacheConfiguration("userCache", 
                        RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(30)))
                .build();
    }
}
```

2. **在服务层添加缓存注解**：

```java
@Service
public class DeviceServiceImpl implements DeviceService {
    
    @Cacheable(value = "deviceCache", key = "#id")
    public Device getDeviceById(Long id) {
        // 数据库查询逻辑
    }
    
    @CachePut(value = "deviceCache", key = "#device.id")
    public Device updateDevice(Device device) {
        // 更新逻辑
    }
    
    @CacheEvict(value = "deviceCache", key = "#id")
    public void deleteDevice(Long id) {
        // 删除逻辑
    }
}
```

## 5. 代码质量管理建议

### 5.1 单元测试

**问题描述**：
- 未看到项目中的单元测试代码。

**优化建议**：
为核心组件编写单元测试，使用JUnit和Mockito：

```java
@ExtendWith(MockitoExtension.class)
public class ServiceDispatcherTest {
    
    @Mock
    private DubboAuthServiceClient authServiceClient;
    
    @Mock
    private DubboDeviceServiceClient deviceServiceClient;
    
    @InjectMocks
    private ServiceDispatcher serviceDispatcher;
    
    @Test
    public void testDispatchLoginRequest() {
        // 准备测试数据
        Map<String, Object> params = new HashMap<>();
        params.put("username", "testuser");
        params.put("password", "password123");
        
        // 模拟服务调用
        LoginResponse response = LoginResponse.newBuilder()
                .setSuccess(true)
                .setToken("test-token")
                .setUsername("testuser")
                .setRole("USER")
                .build();
        Mockito.when(authServiceClient.login("testuser", "password123")).thenReturn(response);
        
        // 执行测试
        CompletableFuture<Map<String, Object>> future = serviceDispatcher.dispatchAuthRequest("login", params);
        
        // 验证结果
        Map<String, Object> result = future.join();
        Assertions.assertTrue((boolean) result.get("success"));
        Assertions.assertEquals("test-token", result.get("token"));
        Mockito.verify(authServiceClient).login("testuser", "password123");
    }
}
```

### 5.2 代码规范

**问题描述**：
- 项目中可能存在不一致的代码规范。

**优化建议**：
1. **集成SonarQube进行代码质量检查**。
2. **使用Checkstyle、PMD等工具进行静态代码分析**。
3. **在IDE中配置统一的代码格式化规则**。

## 6. 部署与运维优化

### 6.1 容器化优化

**问题描述**：
- 虽然项目支持Docker部署，但可能缺少优化的Dockerfile和资源限制配置。

**优化建议**：
使用多阶段构建和优化的Dockerfile：

```dockerfile
# 构建阶段
FROM maven:3.8.6-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 运行阶段
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar

# 配置JVM参数
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 创建非root用户运行应用
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### 6.2 Kubernetes部署优化

**问题描述**：
- 缺少Kubernetes资源限制和健康检查配置。

**优化建议**：
完善Kubernetes部署配置：

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: api-gateway
spec:
  replicas: 3
  selector:
    matchLabels:
      app: api-gateway
  template:
    metadata:
      labels:
        app: api-gateway
    spec:
      containers:
      - name: api-gateway
        image: x-service/api-gateway:1.0.0
        resources:
          requests:
            cpu: "500m"
            memory: "1Gi"
          limits:
            cpu: "1"
            memory: "2Gi"
        ports:
        - containerPort: 8080
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 15
          periodSeconds: 5
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
```

## 7. 总结

本优化方案涵盖了X-Service项目的多个方面，包括：

1. **代码优化**：修复组件工具类问题，优化ServiceDispatcher和Dubbo服务客户端。
2. **架构优化**：统一响应格式，实现全局异常处理，优化网关功能。
3. **性能优化**：数据库索引和连接池优化，缓存策略优化。
4. **代码质量管理**：添加单元测试，统一代码规范。
5. **部署与运维优化**：容器化和Kubernetes部署优化。

通过这些优化，可以显著提高系统的性能、可靠性和可维护性，更好地支持大规模物联网设备的监控与管理需求。