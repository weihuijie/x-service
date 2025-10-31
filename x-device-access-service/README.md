# 设备接入服务 (x-device-access-service)

## 项目概述

`x-device-access-service` 是一个基于Spring Boot、Netty和gRPC实现的设备接入服务，负责处理设备连接、数据接收和设备管理等功能。

## 核心功能

1. **设备连接管理**：处理设备的连接和断开连接
2. **设备数据处理**：接收和处理设备发送的数据
3. **设备状态管理**：管理设备的状态信息
4. **gRPC服务**：提供高性能的gRPC接口供API网关调用
5. **REST API服务**：提供标准的RESTful API接口

## 技术栈

- Spring Boot 2.6.x
- Netty
- gRPC
- Redis
- MongoDB
- Nacos

## 项目结构

```
src/main/java/com/x/device/access/service/
├── DeviceAccessServiceApplication.java    # 应用入口
├── config/
│   └── GrpcConfig.java                   # gRPC配置
├── handler/
│   └── DeviceAccessHandler.java          # 设备接入处理器
├── grpc/
│   ├── DeviceServiceImpl.java            # gRPC服务实现
│   └── DeviceServiceTestController.java  # gRPC测试控制器
├── controller/
│   └── DeviceController.java             # REST API控制器
```

## 配置说明

### 主要配置文件

- `src/main/resources/application.yml` - 核心配置文件

### 关键配置项

1. **服务器配置**
```yaml
server:
  port: 8083

# gRPC配置
grpc:
  server:
    port: 9093
```

2. **数据库配置**
```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/device_access
      
  redis:
    host: localhost
    port: 6379
```

3. **Netty配置**
```yaml
netty:
  port: 8000
```

4. **Dubbo配置**
```yaml
dubbo:
  application:
    name: ${spring.application.name}
  registry:
    address: nacos://nacos:nacos@127.0.0.1:8848
  protocol:
    name: grpc
    port: -1
```

## gRPC接口

### 设备服务接口

1. **CreateDevice (创建设备)**
   - 方法：`rpc CreateDevice (CreateDeviceRequest) returns (CreateDeviceResponse);`
   - 功能：创建新设备

2. **GetDevice (获取设备)**
   - 方法：`rpc GetDevice (GetDeviceRequest) returns (GetDeviceResponse);`
   - 功能：根据ID获取设备信息

3. **UpdateDeviceStatus (更新设备状态)**
   - 方法：`rpc UpdateDeviceStatus (UpdateDeviceStatusRequest) returns (UpdateDeviceStatusResponse);`
   - 功能：更新设备状态

4. **DeleteDevice (删除设备)**
   - 方法：`rpc DeleteDevice (DeleteDeviceRequest) returns (DeleteDeviceResponse);`
   - 功能：删除设备

5. **ListDevices (获取设备列表)**
   - 方法：`rpc ListDevices (ListDevicesRequest) returns (ListDevicesResponse);`
   - 功能：获取设备列表，支持分页和状态筛选

6. **ExecuteOperation (执行操作)**
   - 方法：`rpc ExecuteOperation (ExecuteOperationRequest) returns (ExecuteOperationResponse);`
   - 功能：通用操作执行接口，支持动态调用各种设备操作

### Dubbo gRPC支持

设备服务同时支持通过Dubbo gRPC调用，其他服务可以通过Dubbo注解方式引用该服务：

```java
@DubboReference(version = "1.0.0")
private DeviceServiceGrpc.DeviceServiceBlockingStub deviceService;
```

**注意**: 服务注册与发现使用Nacos作为注册中心，确保Nacos服务正常运行。

## REST API接口

设备接入服务还提供了一套完整的REST API接口，详情请参考 [REST API文档](REST_API.md)。

## gRPC测试接口

为了方便测试Dubbo gRPC调用，项目还提供了以下测试接口：

1. **测试创建设备**
   - 接口：`GET /test/grpc/createDevice`
   - 功能：测试通过Dubbo gRPC创建设备

2. **测试获取设备列表**
   - 接口：`GET /test/grpc/listDevices`
   - 功能：测试通过Dubbo gRPC获取设备列表

**注意**: 测试前确保Nacos注册中心正常运行，设备服务已成功注册到Nacos。

## 使用方法

### 启动服务

```bash
# 使用Maven启动
mvn spring-boot:run

# 或使用打包后的jar文件
java -jar x-device-access-service-1.0.0.jar
```

### 启动前准备

1. 确保Nacos服务正常运行，默认地址: http://127.0.0.1:8848
2. 确保MongoDB服务正常运行
3. 确保Redis服务正常运行

### 访问服务

- RESTful服务默认监听端口：8083
  - 设备管理API: http://localhost:8083/device
- gRPC服务默认监听端口：9093
- Netty服务默认监听端口：8000

## 注意事项

1. 确保MongoDB和Redis服务正常运行
2. 根据实际环境修改数据库连接配置
3. 确保gRPC和Netty端口未被占用
4. 在生产环境中，应加强设备认证和数据安全

## 常见问题

1. **Q: MongoDB连接失败怎么办？**
   A: 检查MongoDB服务是否正常运行，确认连接配置是否正确。

2. **Q: Redis连接失败怎么办？**
   A: 检查Redis服务是否正常运行，确认连接配置是否正确。

3. **Q: gRPC服务启动失败怎么办？**
   A: 检查gRPC端口是否被占用，确认相关依赖是否正确引入。

4. **Q: Netty服务启动失败怎么办？**
   A: 检查Netty端口是否被占用，确认相关配置是否正确。