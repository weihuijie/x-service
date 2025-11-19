# X-Service 开发与部署指南

## 1. 开发环境搭建

### 1.1 环境要求

- **JDK**：JDK 17
- **Maven**：3.8.6 或更高版本
- **Git**：2.30 或更高版本
- **Docker**：20.10 或更高版本
- **Docker Compose**：2.0 或更高版本

### 1.2 环境变量配置

创建 `.env` 文件，配置必要的环境变量：

```properties
# 数据库配置
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_USERNAME=root
MYSQL_PASSWORD=password
MYSQL_DATABASE=x_service

# Redis配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# Kafka配置
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Nacos配置
NACOS_SERVER_ADDR=localhost:8848
NACOS_NAMESPACE=public

# IoTDB配置
IOTDB_HOST=localhost
IOTDB_PORT=6667
IOTDB_USERNAME=root
IOTDB_PASSWORD=root

# 服务配置
SERVER_PORT=8080
SERVICE_NAME=x-service
```

### 1.3 IDE配置

#### 1.3.1 IntelliJ IDEA 配置

1. 安装插件：
   - Lombok
   - Spring Boot Assistant
   - Git Integration
   - Maven Helper

2. 导入项目：
   - 选择 `File -> Open`
   - 选择项目的 `pom.xml` 文件
   - 选择 `Open as Project`

3. Maven配置：
   - 确保使用正确的JDK版本
   - 配置Maven镜像（可选）：
     ```xml
     <mirrors>
         <mirror>
             <id>aliyunmaven</id>
             <mirrorOf>*</mirrorOf>
             <name>阿里云公共仓库</name>
             <url>https://maven.aliyun.com/repository/public</url>
         </mirror>
     </mirrors>
     ```

## 2. 项目结构说明

### 2.1 目录结构

```plaintext
x-service/                  # 项目根目录
├── x-common/              # 公共模块
│   ├── src/main/java/com/x/common/  # 源代码目录
│   │   ├── config/        # 公共配置类
│   │   ├── constant/      # 常量定义
│   │   ├── exception/     # 异常处理
│   │   ├── model/         # 公共数据模型
│   │   ├── util/          # 工具类
│   │   └── annotation/    # 自定义注解
│   └── pom.xml           # Maven配置
├── x-api/                 # API模块
│   ├── src/main/java/com/x/api/     # 源代码目录
│   │   ├── controller/    # REST控制器
│   │   ├── dto/           # 数据传输对象
│   │   └── vo/            # 视图对象
│   └── pom.xml           # Maven配置
├── x-service/             # 服务模块
│   ├── src/main/java/com/x/service/  # 源代码目录
│   │   ├── impl/          # 服务实现类
│   │   └── mapper/        # MyBatis映射器
│   └── pom.xml           # Maven配置
├── x-dao/                 # 数据访问模块
│   ├── src/main/java/com/x/dao/     # 源代码目录
│   │   ├── entity/        # 数据库实体类
│   │   └── repository/    # 数据访问接口
│   └── pom.xml           # Maven配置
├── x-integration/         # 集成模块
│   ├── src/main/java/com/x/integration/  # 源代码目录
│   │   ├── kafka/         # Kafka集成
│   │   ├── redis/         # Redis集成
│   │   └── iotdb/         # IoTDB集成
│   └── pom.xml           # Maven配置
├── x-component/           # 组件模块
│   ├── src/main/java/com/x/component/  # 源代码目录
│   │   ├── cache/         # 缓存组件
│   │   ├── log/           # 日志组件
│   │   └── security/      # 安全组件
│   └── pom.xml           # Maven配置
├── pom.xml                # 项目主Maven配置
├── README.md              # 项目说明文档
├── ARCHITECTURE.md        # 系统架构文档
├── COMPONENTS_GUIDE.md    # 组件使用指南
├── PERFORMANCE_GUIDE.md   # 性能优化指南
└── DEVELOPMENT_GUIDE.md   # 开发与部署指南
```

### 2.2 模块说明

- **x-common**：提供公共配置、工具类、异常处理等基础设施
- **x-api**：对外API接口定义，包括REST控制器和数据传输对象
- **x-service**：业务逻辑层实现，处理核心业务逻辑
- **x-dao**：数据访问层，定义数据库实体和访问接口
- **x-integration**：第三方系统集成，包括消息队列、缓存、数据库等
- **x-component**：可复用组件，如缓存、日志、安全等组件

## 3. 开发规范

### 3.1 编码规范

#### 3.1.1 命名规范

- **包名**：使用小写字母，采用反向域名形式，如 `com.x.common.util`
- **类名**：使用大驼峰命名法，如 `DeviceServiceImpl`
- **方法名**：使用小驼峰命名法，如 `getDeviceById`
- **变量名**：使用小驼峰命名法，如 `deviceName`
- **常量名**：使用全大写字母，单词间用下划线分隔，如 `MAX_CONNECTIONS`
- **接口名**：使用大驼峰命名法，通常以 `I` 开头或直接以业务名称结尾，如 `IDeviceService` 或 `DeviceService`

#### 3.1.2 代码风格

- 缩进使用4个空格
- 每行不超过120个字符
- 方法体不超过50行
- 类不超过300行
- 使用Lombok简化代码（如 `@Data`, `@Slf4j`, `@Service`）

### 3.2 文档规范

#### 3.2.1 JavaDoc规范

```java
/**
 * 获取设备信息
 * <p>
 * 根据设备ID获取设备详细信息，包含设备基本信息、状态信息等
 * </p>
 * @param deviceId 设备ID
 * @return 设备信息对象
 * @throws DeviceNotFoundException 当设备不存在时抛出
 * @since 1.0.0
 */
public DeviceDTO getDeviceById(String deviceId) throws DeviceNotFoundException {
    // 实现代码
}
```

#### 3.2.2 代码注释

- 复杂逻辑必须添加注释说明
- 算法实现需要说明算法原理和复杂度
- 关键业务逻辑需要添加业务说明
- 接口变更需要添加版本变更说明

### 3.3 错误处理规范

#### 3.3.1 异常体系

- **BusinessException**：业务逻辑异常
- **ValidationException**：参数校验异常
- **SystemException**：系统级异常
- **ResourceNotFoundException**：资源未找到异常

#### 3.3.2 异常处理示例

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        logger.warn("业务异常: {}", ex.getMessage(), ex);
        ErrorResponse error = new ErrorResponse("BUSINESS_ERROR", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex) {
        logger.warn("参数校验异常: {}", ex.getMessage(), ex);
        ErrorResponse error = new ErrorResponse("VALIDATION_ERROR", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(SystemException.class)
    public ResponseEntity<ErrorResponse> handleSystemException(SystemException ex) {
        logger.error("系统异常: {}", ex.getMessage(), ex);
        ErrorResponse error = new ErrorResponse("SYSTEM_ERROR", "系统内部错误，请稍后重试");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        logger.error("未知异常: {}", ex.getMessage(), ex);
        ErrorResponse error = new ErrorResponse("UNKNOWN_ERROR", "未知错误，请联系管理员");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

## 4. API开发指南

### 4.1 RESTful API设计

#### 4.1.1 资源命名

- 使用名词表示资源
- 使用复数形式
- 避免使用动词
- 示例：`/api/v1/devices`, `/api/v1/users`

#### 4.1.2 HTTP方法使用

- **GET**：获取资源
- **POST**：创建资源
- **PUT**：更新资源
- **DELETE**：删除资源
- **PATCH**：部分更新资源

#### 4.1.3 状态码使用

- **200 OK**：请求成功
- **201 Created**：资源创建成功
- **204 No Content**：请求成功但无内容返回
- **400 Bad Request**：请求参数错误
- **401 Unauthorized**：未授权
- **403 Forbidden**：禁止访问
- **404 Not Found**：资源不存在
- **500 Internal Server Error**：服务器内部错误

### 4.2 API实现示例

```java
@RestController
@RequestMapping("/api/v1/devices")
@Api(tags = "设备管理")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    @GetMapping("/{deviceId}")
    @ApiOperation("获取设备详情")
    public ResponseEntity<DeviceDTO> getDeviceById(@PathVariable String deviceId) {
        DeviceDTO device = deviceService.getDeviceById(deviceId);
        return ResponseEntity.ok(device);
    }

    @GetMapping
    @ApiOperation("查询设备列表")
    public ResponseEntity<PageResult<DeviceDTO>> queryDevices(
            @RequestParam(required = false) String deviceName,
            @RequestParam(required = false) String deviceType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        DeviceQuery query = new DeviceQuery();
        query.setDeviceName(deviceName);
        query.setDeviceType(deviceType);
        query.setPage(page);
        query.setPageSize(pageSize);
        
        PageResult<DeviceDTO> result = deviceService.queryDevices(query);
        return ResponseEntity.ok(result);
    }

    @PostMapping
    @ApiOperation("创建设备")
    public ResponseEntity<DeviceDTO> createDevice(@Valid @RequestBody DeviceCreateDTO createDTO) {
        DeviceDTO device = deviceService.createDevice(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(device);
    }

    @PutMapping("/{deviceId}")
    @ApiOperation("更新设备")
    public ResponseEntity<DeviceDTO> updateDevice(@PathVariable String deviceId,
                                               @Valid @RequestBody DeviceUpdateDTO updateDTO) {
        DeviceDTO device = deviceService.updateDevice(deviceId, updateDTO);
        return ResponseEntity.ok(device);
    }

    @DeleteMapping("/{deviceId}")
    @ApiOperation("删除设备")
    public ResponseEntity<Void> deleteDevice(@PathVariable String deviceId) {
        deviceService.deleteDevice(deviceId);
        return ResponseEntity.noContent().build();
    }
}
```

### 4.3 API文档生成

使用Swagger生成API文档：

```java
@Configuration
@EnableSwagger2WebMvc
public class SwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.x.api.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("X-Service API文档")
                .description("X-Service物联网平台API接口文档")
                .version("1.0.0")
                .build();
    }
}
```

## 5. 数据库操作

### 5.1 MyBatis使用

#### 5.1.1 Mapper接口定义

```java
@Mapper
public interface DeviceMapper {

    DeviceEntity selectById(String deviceId);
    
    List<DeviceEntity> selectByCondition(@Param("condition") DeviceQuery query,
                                        @Param("offset") int offset,
                                        @Param("limit") int limit);
    
    int countByCondition(@Param("condition") DeviceQuery query);
    
    int insert(DeviceEntity device);
    
    int updateById(DeviceEntity device);
    
    int deleteById(String deviceId);
}
```

#### 5.1.2 XML配置

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.x.service.mapper.DeviceMapper">

    <select id="selectById" resultType="com.x.dao.entity.DeviceEntity">
        SELECT * FROM device WHERE device_id = #{deviceId}
    </select>

    <select id="selectByCondition" resultType="com.x.dao.entity.DeviceEntity">
        SELECT * FROM device
        <where>
            <if test="condition.deviceName != null and condition.deviceName != ''">
                AND device_name LIKE CONCAT('%', #{condition.deviceName}, '%')
            </if>
            <if test="condition.deviceType != null and condition.deviceType != ''">
                AND device_type = #{condition.deviceType}
            </if>
        </where>
        ORDER BY create_time DESC
        LIMIT #{offset}, #{limit}
    </select>

    <select id="countByCondition" resultType="java.lang.Integer">
        SELECT COUNT(*) FROM device
        <where>
            <if test="condition.deviceName != null and condition.deviceName != ''">
                AND device_name LIKE CONCAT('%', #{condition.deviceName}, '%')
            </if>
            <if test="condition.deviceType != null and condition.deviceType != ''">
                AND device_type = #{condition.deviceType}
            </if>
        </where>
    </select>

    <insert id="insert" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO device (device_id, device_name, device_type, status, create_time, update_time)
        VALUES (#{deviceId}, #{deviceName}, #{deviceType}, #{status}, #{createTime}, #{updateTime})
    </insert>

    <update id="updateById">
        UPDATE device
        SET device_name = #{deviceName},
            device_type = #{deviceType},
            status = #{status},
            update_time = #{updateTime}
        WHERE device_id = #{deviceId}
    </update>

    <delete id="deleteById">
        DELETE FROM device WHERE device_id = #{deviceId}
    </delete>
</mapper>
```

### 5.2 事务管理

```java
@Service
public class DeviceServiceImpl implements DeviceService {

    @Autowired
    private DeviceMapper deviceMapper;
    
    @Autowired
    private DeviceHistoryService deviceHistoryService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public DeviceDTO updateDevice(String deviceId, DeviceUpdateDTO updateDTO) {
        // 1. 验证设备是否存在
        DeviceEntity device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw new DeviceNotFoundException("设备不存在: " + deviceId);
        }
        
        // 2. 更新设备信息
        device.setDeviceName(updateDTO.getDeviceName());
        device.setDeviceType(updateDTO.getDeviceType());
        device.setStatus(updateDTO.getStatus());
        device.setUpdateTime(new Date());
        deviceMapper.updateById(device);
        
        // 3. 记录设备变更历史
        DeviceHistoryDTO historyDTO = new DeviceHistoryDTO();
        historyDTO.setDeviceId(deviceId);
        historyDTO.setOperationType("UPDATE");
        historyDTO.setContent(JSON.toJSONString(updateDTO));
        deviceHistoryService.saveHistory(historyDTO);
        
        // 4. 返回更新后的设备信息
        return convertToDTO(device);
    }
}
```

## 6. 缓存使用

### 6.1 Redis缓存操作

#### 6.1.1 缓存工具类

```java
@Component
public class RedisCacheUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 设置缓存
    public void set(String key, Object value, long expireTime, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, expireTime, timeUnit);
    }

    // 获取缓存
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? (T) value : null;
    }

    // 删除缓存
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    // 批量删除缓存
    public void delete(Collection<String> keys) {
        redisTemplate.delete(keys);
    }

    // 检查缓存是否存在
    public boolean exists(String key) {
        return redisTemplate.hasKey(key);
    }

    // 设置过期时间
    public boolean expire(String key, long expireTime, TimeUnit timeUnit) {
        return redisTemplate.expire(key, expireTime, timeUnit);
    }
}
```

#### 6.1.2 缓存使用示例

```java
@Service
public class DeviceServiceImpl implements DeviceService {

    private static final String DEVICE_CACHE_KEY_PREFIX = "device:info:";
    private static final long DEVICE_CACHE_EXPIRE = 10;
    private static final TimeUnit DEVICE_CACHE_TIME_UNIT = TimeUnit.MINUTES;

    @Autowired
    private DeviceMapper deviceMapper;
    
    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Override
    public DeviceDTO getDeviceById(String deviceId) {
        // 1. 尝试从缓存获取
        String cacheKey = DEVICE_CACHE_KEY_PREFIX + deviceId;
        DeviceDTO cachedDevice = redisCacheUtil.get(cacheKey, DeviceDTO.class);
        if (cachedDevice != null) {
            return cachedDevice;
        }
        
        // 2. 缓存不存在，从数据库查询
        DeviceEntity device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw new DeviceNotFoundException("设备不存在: " + deviceId);
        }
        
        // 3. 转换为DTO
        DeviceDTO deviceDTO = convertToDTO(device);
        
        // 4. 缓存结果
        redisCacheUtil.set(cacheKey, deviceDTO, DEVICE_CACHE_EXPIRE, DEVICE_CACHE_TIME_UNIT);
        
        return deviceDTO;
    }

    @Override
    public void updateDeviceStatus(String deviceId, String status) {
        // 1. 更新数据库
        deviceMapper.updateStatus(deviceId, status);
        
        // 2. 清除缓存
        String cacheKey = DEVICE_CACHE_KEY_PREFIX + deviceId;
        redisCacheUtil.delete(cacheKey);
    }
}
```

## 7. 消息队列使用

### 7.1 Kafka消息生产

```java
@Service
public class KafkaMessageProducer {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    // 发送普通消息
    public void sendMessage(String topic, Object message) {
        kafkaTemplate.send(topic, message);
    }

    // 发送带键的消息
    public void sendMessage(String topic, String key, Object message) {
        kafkaTemplate.send(topic, key, message);
    }

    // 发送带分区的消息
    public void sendMessage(String topic, int partition, String key, Object message) {
        kafkaTemplate.send(topic, partition, key, message);
    }

    // 同步发送消息
    public SendResult<String, Object> sendSyncMessage(String topic, Object message) throws ExecutionException, InterruptedException {
        return kafkaTemplate.send(topic, message).get();
    }

    // 异步发送消息
    public void sendAsyncMessage(String topic, Object message, KafkaCallback callback) {
        kafkaTemplate.send(topic, message).addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {
            @Override
            public void onSuccess(SendResult<String, Object> result) {
                callback.onSuccess(result);
            }

            @Override
            public void onFailure(Throwable ex) {
                callback.onFailure(ex);
            }
        });
    }

    // 回调接口
    public interface KafkaCallback {
        void onSuccess(SendResult<String, Object> result);
        void onFailure(Throwable ex);
    }
}
```

### 7.2 Kafka消息消费

```java
@Component
public class DeviceDataConsumer {

    private static final Logger logger = LoggerFactory.getLogger(DeviceDataConsumer.class);

    @Autowired
    private IoTDBComponentUtil iotdbComponentUtil;

    @KafkaListener(topics = "device-data-topic", groupId = "data-collection-group")
    public void consumeDeviceData(DeviceData data) {
        try {
            logger.info("接收到设备数据: deviceId={}, measurement={}, value={}", 
                      data.getDeviceId(), data.getMeasurement(), data.getValue());
            
            // 处理设备数据，如写入IoTDB
            iotdbComponentUtil.insertData(data);
            
        } catch (Exception e) {
            logger.error("处理设备数据失败: {}", e.getMessage(), e);
            // 可以将失败数据发送到死信队列或进行其他处理
        }
    }

    // 批量消费示例
    @KafkaListener(topics = "device-data-topic", groupId = "data-collection-batch-group")
    public void consumeBatchDeviceData(List<DeviceData> dataList, Acknowledgment acknowledgment) {
        try {
            logger.info("接收到批量设备数据，数量: {}", dataList.size());
            
            // 批量处理数据
            iotdbComponentUtil.insertBatch(dataList);
            
            // 手动提交偏移量
            acknowledgment.acknowledge();
        } catch (Exception e) {
            logger.error("批量处理设备数据失败: {}", e.getMessage(), e);
            // 处理异常
        }
    }
}
```

## 8. 单元测试

### 8.1 测试框架配置

```xml
<!-- pom.xml 依赖 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <scope>test</scope>
</dependency>
```

### 8.2 服务层测试示例

```java
@SpringBootTest
public class DeviceServiceImplTest {

    @Autowired
    private DeviceServiceImpl deviceService;

    @MockBean
    private DeviceMapper deviceMapper;

    @MockBean
    private RedisCacheUtil redisCacheUtil;

    @Test
    public void testGetDeviceById() {
        // 准备测试数据
        String deviceId = "device-001";
        DeviceEntity deviceEntity = new DeviceEntity();
        deviceEntity.setDeviceId(deviceId);
        deviceEntity.setDeviceName("测试设备");
        deviceEntity.setDeviceType("temperature");
        deviceEntity.setStatus("online");

        // 模拟依赖行为
        when(redisCacheUtil.get(anyString(), eq(DeviceDTO.class))).thenReturn(null);
        when(deviceMapper.selectById(deviceId)).thenReturn(deviceEntity);

        // 执行测试
        DeviceDTO deviceDTO = deviceService.getDeviceById(deviceId);

        // 验证结果
        assertNotNull(deviceDTO);
        assertEquals(deviceId, deviceDTO.getDeviceId());
        assertEquals("测试设备", deviceDTO.getDeviceName());

        // 验证交互
        verify(redisCacheUtil).get(eq("device:info:" + deviceId), eq(DeviceDTO.class));
        verify(deviceMapper).selectById(deviceId);
        verify(redisCacheUtil).set(anyString(), any(DeviceDTO.class), anyLong(), any(TimeUnit.class));
    }

    @Test
    public void testGetDeviceById_NotFound() {
        // 准备测试数据
        String deviceId = "device-not-exist";

        // 模拟依赖行为
        when(redisCacheUtil.get(anyString(), eq(DeviceDTO.class))).thenReturn(null);
        when(deviceMapper.selectById(deviceId)).thenReturn(null);

        // 执行测试并验证异常
        DeviceNotFoundException exception = assertThrows(DeviceNotFoundException.class, () -> {
            deviceService.getDeviceById(deviceId);
        });

        // 验证异常信息
        assertTrue(exception.getMessage().contains(deviceId));
    }
}
```

## 9. 部署指南

### 9.1 Docker部署

#### 9.1.1 Dockerfile示例

```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/x-service.jar /app/app.jar

EXPOSE 8080

ENV JAVA_OPTS="-Xms1024m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
```

#### 9.1.2 Docker Compose配置

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: x-service-mysql
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: password
      MYSQL_DATABASE: x_service
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql

  redis:
    image: redis:6.2
    container_name: x-service-redis
    restart: always
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data

  kafka:
    image: confluentinc/cp-kafka:7.0.0
    container_name: x-service-kafka
    restart: always
    ports:
      - "9092:9092"
    environment:
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
    depends_on:
      - zookeeper

  zookeeper:
    image: confluentinc/cp-zookeeper:7.0.0
    container_name: x-service-zookeeper
    restart: always
    ports:
      - "2181:2181"
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181

  nacos:
    image: nacos/nacos-server:2.0.3
    container_name: x-service-nacos
    restart: always
    ports:
      - "8848:8848"
    environment:
      MODE: standalone

  iotdb:
    image: apache/iotdb:0.13.3
    container_name: x-service-iotdb
    restart: always
    ports:
      - "6667:6667"
    volumes:
      - iotdb-data:/iotdb/data

  x-service:
    build: .
    container_name: x-service
    restart: always
    ports:
      - "8080:8080"
    depends_on:
      - mysql
      - redis
      - kafka
      - nacos
      - iotdb
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/x_service?useSSL=false&serverTimezone=UTC
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: password
      SPRING_REDIS_HOST: redis
      SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:29092
      SPRING_CLOUD_NACOS_DISCOVERY_SERVER_ADDR: nacos:8848
      IOTDB_HOST: iotdb

volumes:
  mysql-data:
  redis-data:
  iotdb-data:
```

### 9.2 Kubernetes部署

#### 9.2.1 Deployment配置

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: x-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: x-service
  template:
    metadata:
      labels:
        app: x-service
    spec:
      containers:
      - name: x-service
        image: x-service:latest
        ports:
        - containerPort: 8080
        resources:
          limits:
            cpu: "2"
            memory: "2Gi"
          requests:
            cpu: "1"
            memory: "1Gi"
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            secretKeyRef:
              name: x-service-secrets
              key: db-url
        - name: SPRING_DATASOURCE_USERNAME
          valueFrom:
            secretKeyRef:
              name: x-service-secrets
              key: db-username
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: x-service-secrets
              key: db-password
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
```

#### 9.2.2 Service配置

```yaml
apiVersion: v1
kind: Service
metadata:
  name: x-service
spec:
  selector:
    app: x-service
  ports:
  - port: 80
    targetPort: 8080
  type: ClusterIP
```

#### 9.2.3 Ingress配置

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: x-service-ingress
  annotations:
    kubernetes.io/ingress.class: nginx
spec:
  rules:
  - host: api.x-service.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: x-service
            port:
              number: 80
```

## 10. 持续集成与持续部署

### 10.1 CI/CD流程

1. **代码提交**：开发者将代码提交到Git仓库
2. **自动化构建**：Jenkins/GitLab CI自动触发构建流程
3. **代码检查**：运行SonarQube进行代码质量检查
4. **单元测试**：运行JUnit测试，确保代码质量
5. **构建镜像**：构建Docker镜像并推送到镜像仓库
6. **部署测试环境**：将镜像部署到测试环境
7. **集成测试**：在测试环境运行集成测试
8. **部署生产环境**：通过审批后部署到生产环境

### 10.2 Jenkins Pipeline示例

```groovy
pipeline {
    agent any
    
    environment {
        DOCKER_REPO = 'registry.example.com/x-service'
        DOCKER_TAG = "${env.BUILD_NUMBER}"
        KUBECONFIG = credentials('kubeconfig')
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }
        
        stage('Code Quality') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh 'mvn sonar:sonar'
                }
            }
        }
        
        stage('Unit Test') {
            steps {
                sh 'mvn test'
                junit '**/target/surefire-reports/*.xml'
            }
        }
        
        stage('Build Docker Image') {
            steps {
                sh "docker build -t ${DOCKER_REPO}:${DOCKER_TAG} ."
                sh "docker tag ${DOCKER_REPO}:${DOCKER_TAG} ${DOCKER_REPO}:latest"
            }
        }
        
        stage('Push Docker Image') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-registry', passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
                    sh "docker login -u ${DOCKER_USERNAME} -p ${DOCKER_PASSWORD} ${DOCKER_REPO.split('/')[0]}"
                    sh "docker push ${DOCKER_REPO}:${DOCKER_TAG}"
                    sh "docker push ${DOCKER_REPO}:latest"
                }
            }
        }
        
        stage('Deploy to Test') {
            steps {
                sh "sed -i 's|image:.*|image: ${DOCKER_REPO}:${DOCKER_TAG}|g' k8s/test/deployment.yaml"
                sh "kubectl --kubeconfig=${KUBECONFIG} apply -f k8s/test/"
            }
        }
        
        stage('Integration Test') {
            steps {
                sh 'mvn verify -P integration-test'
            }
        }
        
        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            steps {
                input message: '确认部署到生产环境？', ok: '部署'
                sh "sed -i 's|image:.*|image: ${DOCKER_REPO}:${DOCKER_TAG}|g' k8s/prod/deployment.yaml"
                sh "kubectl --kubeconfig=${KUBECONFIG} apply -f k8s/prod/"
            }
        }
    }
    
    post {
        success {
            echo '构建成功！'
            mail to: 'team@example.com', subject: '构建成功', body: '构建 #${BUILD_NUMBER} 已成功完成。'
        }
        failure {
            echo '构建失败！'
            mail to: 'team@example.com', subject: '构建失败', body: '构建 #${BUILD_NUMBER} 失败。请检查构建日志。'
        }
        always {
            cleanWs()
        }
    }
}
```

## 11. 日志与监控

### 11.1 日志配置

#### 11.1.1 Logback配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/x-service.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>logs/x-service.%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
            <maxFileSize>100MB</maxFileSize>
            <maxHistory>30</maxHistory>
            <totalSizeCap>20GB</totalSizeCap>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE" />
        <appender-ref ref="FILE" />
    </root>

    <!-- 特定包的日志级别 -->
    <logger name="com.x" level="DEBUG" />
    <logger name="org.springframework" level="INFO" />
    <logger name="org.hibernate" level="WARN" />
</configuration>
```

#### 11.1.2 日志使用示例

```java
@Service
public class DeviceServiceImpl implements DeviceService {

    private static final Logger logger = LoggerFactory.getLogger(DeviceServiceImpl.class);

    @Override
    public DeviceDTO getDeviceById(String deviceId) {
        logger.debug("获取设备信息，设备ID: {}", deviceId);
        try {
            DeviceEntity device = deviceMapper.selectById(deviceId);
            if (device == null) {
                logger.warn("设备不存在，设备ID: {}", deviceId);
                throw new DeviceNotFoundException("设备不存在: " + deviceId);
            }
            logger.info("获取设备信息成功，设备ID: {}, 设备名称: {}", deviceId, device.getDeviceName());
            return convertToDTO(device);
        } catch (DeviceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("获取设备信息异常，设备ID: {}", deviceId, e);
            throw new SystemException("获取设备信息失败", e);
        }
    }
}
```

### 11.2 监控配置

#### 11.2.1 Spring Boot Actuator配置

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when_authorized
      probes:
        enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: ${spring.application.name}
```

#### 11.2.2 Prometheus配置

```yaml
scrape_configs:
  - job_name: 'x-service'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s
    static_configs:
      - targets: ['x-service:8080']
```

#### 11.2.3 Grafana Dashboard

创建Grafana Dashboard监控以下指标：

- CPU使用率
- 内存使用率
- 请求响应时间
- 请求量
- 错误率
- 数据库连接数
- 缓存命中率
- Kafka消息积压数

## 12. 安全最佳实践

### 12.1 认证与授权

- 使用OAuth2 + JWT进行认证和授权
- 实现基于角色的访问控制（RBAC）
- 定期刷新令牌
- 设置合理的令牌过期时间

### 12.2 数据安全

- 敏感数据加密存储
- 使用HTTPS协议传输数据
- 实现数据脱敏
- 定期备份数据

### 12.3 输入验证

- 使用Bean Validation进行参数校验
- 防止SQL注入
- 防止XSS攻击
- 防止CSRF攻击

### 12.4 安全配置示例

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            .antMatchers("/api/v1/auth/**").permitAll()
            .antMatchers("/actuator/health/**").permitAll()
            .antMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
            .anyRequest().authenticated()
            .and()
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling()
            .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
            .accessDeniedHandler(new JwtAccessDeniedHandler());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

## 13. 常见问题排查

### 13.1 性能问题排查

1. **检查监控指标**：查看CPU、内存、磁盘I/O等指标
2. **分析日志**：查找错误日志和慢日志
3. **性能分析**：使用JProfiler或Arthas进行性能分析
4. **数据库优化**：检查慢查询，优化索引
5. **缓存策略**：检查缓存命中率，优化缓存策略

### 13.2 连接问题排查

1. **网络连接**：检查网络连通性
2. **端口占用**：检查端口是否被占用
3. **连接池配置**：检查连接池大小和超时设置
4. **防火墙设置**：检查防火墙是否阻止连接

### 13.3 数据一致性问题排查

1. **事务日志**：检查事务日志，确认事务执行情况
2. **数据校验**：对比不同数据源的数据
3. **错误重试机制**：检查错误重试逻辑是否合理
4. **分布式事务**：如果使用分布式事务，检查事务协调器配置

## 14. 版本发布流程

1. **需求分析**：收集和分析用户需求
2. **开发计划**：制定开发计划和里程碑
3. **功能开发**：开发新功能和修复bug
4. **代码审核**：进行代码审核，确保代码质量
5. **测试**：进行单元测试、集成测试和系统测试
6. **版本发布**：发布新版本
7. **监控与反馈**：监控系统运行状态，收集用户反馈
8. **迭代优化**：根据用户反馈进行优化迭代

## 15. 总结

本开发与部署指南提供了X-Service项目的完整开发流程和最佳实践，包括环境搭建、项目结构、开发规范、API设计、数据库操作、缓存使用、消息队列、测试、部署、CI/CD、日志监控和安全最佳实践等内容。

遵循本指南可以帮助开发团队提高开发效率、保证代码质量、确保系统稳定性和安全性。在实际开发过程中，应根据具体项目需求和团队情况进行适当调整和优化。