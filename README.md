# X-Service 物联网设备监控与管理系统

## 项目简介

X-Service 是一个基于微服务架构的物联网设备监控与管理系统，旨在为物联网设备提供全面的接入、监控、管理和数据分析能力。系统采用现代化的技术栈，支持大规模设备接入和实时数据处理。

### 核心特性

- **设备管理**：支持设备全生命周期管理，包括注册、配置、监控和注销
- **数据采集与处理**：实时采集设备数据，支持清洗、存储和分析
- **实时分析**：基于 Flink 进行设备数据的实时计算和分析
- **离线分析**：支持大规模数据的批处理分析和报表生成
- **告警通知**：灵活的告警规则配置和多渠道通知
- **API 网关**：统一的接口接入和权限管理
- **微服务架构**：基于 Spring Cloud 的微服务设计，支持高可用和水平扩展

## 技术栈

- **核心框架**：Spring Boot 2.7.x, Spring Cloud 2023.0.x, Spring Cloud Alibaba 2022.0.0
- **数据库**：MySQL 8.0+, MongoDB 4.4+, IoTDB 0.13.3
- **缓存**：Redis 6.2.x+
- **消息队列**：Kafka 3.3.1, RabbitMQ 5.16.0
- **微服务组件**：Nacos 2.2.0, Spring Cloud Gateway
- **通信框架**：Netty 4.1.90.Final, Dubbo 3.2.5, gRPC
- **大数据处理**：Hadoop 3.3.4, Flink 1.16.1
- **任务调度**：XXL-JOB 2.3.1
- **对象存储**：MinIO 8.5.1
- **监控日志**：Prometheus, Grafana, ELK Stack

## 快速开始

### 环境准备

1. **JDK 环境**：JDK 1.8 或更高版本
2. **Maven**：3.6.0 或更高版本
3. **数据库**：MySQL 8.0+, MongoDB 4.4+, IoTDB 0.13.3
4. **中间件**：Redis 6.2.x+, Kafka 3.3.1, RabbitMQ 5.16.0, Nacos 2.2.0
5. **大数据组件**（可选）：Hadoop 3.3.4, Flink 1.16.1

### 启动步骤

1. **克隆项目**
   ```bash
   git clone [项目地址]
   cd x-service
   ```

2. **编译打包**
   ```bash
   mvn clean package -DskipTests
   ```

3. **启动中间件服务**
   - 启动 Nacos 服务
   - 启动 MySQL、MongoDB、IoTDB 数据库
   - 启动 Redis、Kafka、RabbitMQ

4. **启动核心服务**
   按照依赖关系启动服务（具体顺序可参考 ARCHITECTURE.md）：
   - 首先启动基础服务（Config Center、Auth Service 等）
   - 然后启动业务服务（Device Manage Service、Data Collection Service 等）
   - 最后启动网关和前端服务

   启动命令示例：
   ```bash
   java -jar x-auth-service.jar
   java -jar x-device-manage-service.jar
   java -jar x-api-gateway-service.jar
   ```

## 文档导航

详细信息请参考以下文档：

- **[ARCHITECTURE.md](ARCHITECTURE.md)** - 系统架构详细说明
- **[COMPONENTS_GUIDE.md](COMPONENTS_GUIDE.md)** - 组件工具类和中间件使用指南
- **[PERFORMANCE_GUIDE.md](PERFORMANCE_GUIDE.md)** - 性能优化和容量分析
- **[DEVELOPMENT_GUIDE.md](DEVELOPMENT_GUIDE.md)** - 开发指南和最佳实践

## 系统架构概述

X-Service 采用微服务架构设计，包含 14 个核心服务模块，涵盖设备接入、数据采集、处理分析、告警通知等完整功能链路。系统通过 Nacos 实现服务注册发现和配置管理，使用 Kafka 和 RabbitMQ 处理消息流转，采用 MySQL、MongoDB 和 IoTDB 存储不同类型的数据，支持大规模设备接入和实时数据处理。

详细的架构设计请参考 [ARCHITECTURE.md](ARCHITECTURE.md)。

## 服务模块简介

系统包含以下核心服务模块：

1. **Manage Service** - 应用交互中间层，处理页面请求并适配各个服务
2. **Repository Service** - 处理数据库交互和连接
3. **Config Center Service** - 统一管理系统配置
4. **Device Access Service** - 管理物联网设备的接入、认证和协议转换
5. **Device Manage Service** - 设备全生命周期管理
6. **Data Collection Service** - 从 Kafka 消费设备时序数据并进行清洗存储
7. **Data Sync Service** - 定时将 IoTDB 数据同步到 HDFS
8. **API Gateway Service** - 统一接入层，路由请求到后端服务
9. **Auth Service** - 用户认证和授权管理
10. **Real-time Analysis Service** - 基于 Flink 对设备数据进行实时计算
11. **Offline Analysis Service** - 调度 Hadoop 批处理任务生成离线报表
12. **Alert Notice Service** - 管理告警规则并触发通知
13. **Task Scheduler Service** - 管理定时任务
14. **File Service** - 管理设备固件和配置文件等二进制资源

详细的服务模块说明请参考 [ARCHITECTURE.md](ARCHITECTURE.md)。

## 性能指标

- **系统承载能力**：支持 20,000 台设备同时接入
- **数据处理能力**：总数据处理能力达到 10,000,000 点/秒
- **峰值处理能力**：峰值情况下可处理 25,000,000 点/秒
- **可用性**：系统整体可用性达到 99.9%
- **响应时间**：关键操作响应时间 < 10ms

详细的性能优化建议请参考 [PERFORMANCE_GUIDE.md](PERFORMANCE_GUIDE.md)。

## 开发与贡献

请参考 [DEVELOPMENT_GUIDE.md](DEVELOPMENT_GUIDE.md) 了解开发规范、常见问题修复和最佳实践。

## 许可证

[添加适当的许可证信息]