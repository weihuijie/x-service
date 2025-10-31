# Data Collection Service Demo

## 项目概述

这是一个数据收集服务的演示项目，实现了以下功能：

1. 通过Dubbo gRPC调用Device Access Service获取设备列表
2. 根据设备的PLC Code读取PLC数据
3. 将收集到的数据发送到Kafka

## 核心功能

### 1. 设备数据自动收集
- 每30秒自动从Device Access Service获取设备列表
- 并行处理每个设备的数据收集任务
- 通过PLC模拟器读取设备数据
- 将数据发送到Kafka

### 2. 手动触发数据收集
- 提供REST API手动触发数据收集任务
- API端点: `GET /api/data/collect`

## 技术栈

- Spring Boot
- Dubbo gRPC
- Kafka
- Nacos

## 项目结构

```
src/main/java/com/x/data/collection/service/
├── DataCollectionServiceApplication.java  # 应用入口
├── config/
│   └── DubboGrpcConfig.java              # Dubbo gRPC配置
├── collector/
│   └── DeviceDataCollector.java          # 设备数据收集器
├── plc/
│   └── PlcDataSimulator.java             # PLC数据模拟器
├── controller/
│   └── DataCollectionController.java     # 数据收集控制器
└── channel/
    └── kafka/
        ├── DataCollectorForKafka.java    # Kafka数据收集器
        └── HighPerformanceDataCollector.java # 高性能数据收集器
```

## 配置说明

### 主要配置文件

- `src/main/resources/application.yml` - 核心配置文件

### 关键配置项

1. **服务器配置**
```yaml
server:
  port: 8085
```

2. **Dubbo配置**
```yaml
dubbo:
  application:
    name: ${spring.application.name}
  registry:
    address: nacos://nacos:nacos@127.0.0.1:8848
  protocol:
    name: grpc
    port: -1
  consumer:
    timeout: 5000
```

3. **Kafka配置**
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: data-collection-group
```

**注意**: 服务注册与发现使用Nacos作为注册中心，请确保Nacos服务正常运行。

## 使用方法

### 启动服务

```bash
# 使用Maven启动
mvn spring-boot:run

# 或使用打包后的jar文件
java -jar x-data-collection-service-1.0.0.jar
```

### 启动前准备

1. 确保Nacos服务正常运行，默认地址: http://127.0.0.1:8848
2. 确保Kafka服务正常运行
3. 确保IoTDB服务正常运行

### 触发数据收集

1. 自动触发：服务启动后每30秒自动执行一次数据收集任务
2. 手动触发：调用API接口 `GET /api/data/collect`

## 工作流程

1. **获取设备列表**：通过Dubbo gRPC调用Device Access Service的ListDevices接口
2. **读取PLC数据**：使用PlcDataSimulator模拟从PLC读取数据
3. **发送数据到Kafka**：使用现有的DataCollectorForKafka将数据发送到Kafka

## 扩展建议

1. **真实PLC集成**：替换PlcDataSimulator为真实的PLC通信库
2. **数据验证**：添加数据验证逻辑确保数据质量
3. **错误处理**：增强错误处理机制，添加重试逻辑
4. **监控指标**：添加更多的监控指标便于运维
5. **批量处理**：优化批量数据处理性能