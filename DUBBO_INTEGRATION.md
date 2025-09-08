# Dubbo gRPC 集成说明

## 概述

本项目采用 Dubbo gRPC 方式替代原有的纯 gRPC 调用方式，以提供更好的服务治理能力，包括服务发现、负载均衡、容错处理等。

## 架构设计

### 模块结构

1. **x-grpc-common**: 包含.proto文件和生成的gRPC代码
3. **服务提供者**: 如x-device-manage-service、x-auth-service等
4. **服务消费者**: 如x-manage-service等

### 调用方式

采用Dubbo gRPC协议进行服务间通信，通过Zookeeper进行服务注册与发现。

## 服务接口定义

### DeviceService (设备管理服务)

- CreateDevice: 创建设备
- GetDevice: 获取设备信息
- UpdateDeviceStatus: 更新设备状态
- DeleteDevice: 删除设备
- ListDevices: 获取设备列表

### AuthService (认证服务)

- Login: 用户登录
- ValidateToken: 验证Token
- CheckPermission: 检查权限

### DataService (数据处理服务)

- SendToKafka: 发送数据到Kafka
- SendToRabbitMQ: 发送数据到RabbitMQ
- StoreToMinIO: 存储数据到MinIO
- ProcessDataAsync: 异步处理数据

## 配置说明

### 服务提供者配置

在application.yml中添加以下配置：

```yaml
dubbo:
  application:
    name: ${spring.application.name}
  registry:
    address: zookeeper://127.0.0.1:2181
  protocol:
    name: grpc
    port: -1
```

### 服务消费者配置

在application.yml中添加以下配置：

```yaml
dubbo:
  application:
    name: ${spring.application.name}
  registry:
    address: zookeeper://127.0.0.1:2181
```

## 依赖版本

本项目使用以下Dubbo相关依赖版本：

- dubbo-bom: 3.2.5
- dubbo-spring-boot-starter: 3.2.5

确保所有模块使用相同的Dubbo版本，以避免依赖冲突。

## 使用方法

### 服务提供者实现

1. 实现对应的Dubbo服务接口
2. 使用@DubboService注解标记服务实现类
3. 启动服务，自动注册到Zookeeper

### 服务消费者调用

1. 在消费者类中使用@DubboReference注解引用远程服务
2. 直接调用服务方法，如同本地调用

## 优势

1. **服务治理**: 提供服务发现、负载均衡、容错处理等能力
2. **性能**: 基于gRPC协议，具有高性能特点
3. **兼容性**: 兼容原有的gRPC接口定义
4. **监控**: 集成Dubbo监控能力，便于服务治理

## 注意事项

1. 确保Zookeeper服务正常运行
2. 注意服务版本管理和分组配置
3. 合理配置超时时间和重试策略
4. 关注服务调用的异常处理
5. 确保所有模块使用相同的Dubbo版本，避免依赖冲突
