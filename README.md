# X-Service 物联网设备监控与管理系统

这是一个基于微服务架构的物联网设备监控与管理系统，包含14个独立的服务模块。

## 项目结构

```
x-service/
├── x-manage-service              # 应用交互中间层
├── x-repository-service          # 数据库交互和连接处理
├── x-config-center-service       # 统一管理系统配置
├── x-device-access-service       # 物联网设备接入、认证和协议转换
├── x-device-manage-service       # 设备全生命周期管理
├── x-data-collection-service     # 从Kafka消费设备时序数据并进行清洗
├── x-data-sync-service           # 定时将IoTDB数据同步到HDFS
├── x-api-gateway-service         # 统一接入层，路由请求到后端服务
├── x-auth-service                # 用户认证和授权管理
├── x-realtime-analysis-service   # 基于Flink对设备数据进行实时计算
├── x-offline-analysis-service    # 调度Hadoop批处理任务生成离线报表
├── x-alert-notice-service        # 管理告警规则并触发通知
├── x-task-scheduler-service      # 管理定时任务
└── x-file-service                # 管理设备固件和配置文件等二进制资源
```

## 技术栈

- **核心框架**: Spring Boot, Spring Cloud
- **数据库**: MySQL, MongoDB, IoTDB
- **缓存**: Redis
- **消息队列**: Kafka, RabbitMQ
- **网关**: Spring Cloud Gateway
- **配置中心**: Nacos
- **认证授权**: Spring Security, OAuth2, JWT
- **实时计算**: Flink
- **批处理**: Hadoop
- **任务调度**: XXL-JOB
- **文件存储**: MinIO
- **容器化**: Docker, Kubernetes
- **监控**: Prometheus, Grafana
- **日志**: ELK (Elasticsearch, Logstash, Kibana)

## 快速开始

### 环境准备

1. 安装JDK 17+
2. 安装Maven 3.6+
3. 安装Docker和Docker Compose

### 启动基础服务

```bash
# 启动基础依赖服务
docker-compose up -d
```

### 构建和运行服务

```bash
# 构建所有服务
mvn clean install

# 运行各个服务
cd x-manage-service && mvn spring-boot:run
# 在新的终端窗口中运行其他服务
```

或者使用Docker运行:

```bash
# 为每个服务构建Docker镜像
cd x-manage-service && docker build -t manage-service .
# 为其他服务执行相同操作

# 使用Docker Compose启动所有服务
docker-compose up -d

# 使用Kubernetes部署
kubectl apply -f x-manage-service/k8s-deployment.yaml
# 为其他服务执行相同操作
```

## 模块详细说明

### 1. Manage Service (x-manage-service)
应用交互中间层，处理页面请求并适配各个服务。

### 2. Repository Service (x-repository-service)
处理数据库交互和连接。

### 3. Config Center Service (x-config-center-service)
统一管理系统配置，基于Nacos实现。

### 4. Device Access Service (x-device-access-service)
管理物联网设备的接入、认证和协议转换，基于Netty实现。

### 5. Device Manage Service (x-device-manage-service)
设备全生命周期管理，包括设备的注册、配置、监控和注销。

### 6. Data Collection Service (x-data-collection-service)
从Kafka消费设备时序数据并进行清洗，然后存储到IoTDB。

### 7. Data Sync Service (x-data-sync-service)
定时将IoTDB数据同步到HDFS，供离线分析使用。

### 8. API Gateway Service (x-api-gateway-service)
统一接入层，路由请求到后端服务并进行权限校验。

### 9. Auth Service (x-auth-service)
用户认证和授权管理，基于JWT实现。

### 10. Real-time Analysis Service (x-realtime-analysis-service)
基于Flink对设备数据进行实时计算。

### 11. Offline Analysis Service (x-offline-analysis-service)
调度Hadoop批处理任务生成离线报表。

### 12. Alert Notice Service (x-alert-notice-service)
管理告警规则并触发通知，支持邮件、短信等多种通知方式。

### 13. Task Scheduler Service (x-task-scheduler-service)
管理定时任务，基于XXL-JOB实现。

### 14. File Service (x-file-service)
管理设备固件和配置文件等二进制资源，基于MinIO实现。

## 组件工具类

为了简化各种中间件组件的使用，项目在[x-common](file:///D:/work/project/me/x-service/x-common)模块中提供了统一的组件工具类。
详细说明请参考[COMPONENT_UTILS.md](file:///D:/work/project/me/x-service/COMPONENT_UTILS.md)文件。

## 安全机制

系统采用基于JWT的认证和授权机制：

1. 用户通过Auth Service进行身份验证
2. Auth Service生成包含用户角色信息的JWT Token
3. 客户端在后续请求中携带Token
4. API Gateway验证Token有效性
5. 各个服务根据用户角色控制访问权限

## 监控和日志

系统集成了完整的监控和日志解决方案：

### Prometheus + Grafana 监控体系
- 每个服务都暴露了Prometheus指标端点
- 通过Prometheus收集各服务的性能指标
- 使用Grafana进行可视化展示
- 监控内容包括JVM指标、HTTP请求统计、业务指标等

### ELK日志体系
- 使用Logback将日志输出为JSON格式
- 通过Logstash收集和处理日志
- 使用Elasticsearch存储和索引日志
- 通过Kibana进行日志查询和分析

## 性能测试

项目包含了使用Apache JMeter进行性能测试的测试计划：

- 模拟设备数据上报的并发测试
- 设备管理接口的负载测试
- 可根据实际需求调整测试参数

执行性能测试：
```bash
# 使用JMeter GUI模式打开测试计划
jmeter -t jmeter-test-plan.jmx

# 或使用命令行模式运行测试
jmeter -n -t jmeter-test-plan.jmx -l results.jtl
```

## 高性能架构设计

为了满足大规模设备接入需求（如2万台设备，每台设备500个点位，每秒读取一次），系统采用了以下高性能架构设计：

### 1. 数据收集服务优化

- **批量处理**：使用高性能批量处理机制，减少数据库交互次数
- **异步处理**：采用异步处理模型，提高系统吞吐量
- **线程池优化**：配置合理的线程池大小，充分利用系统资源
- **连接池管理**：优化IoTDB和MySQL连接池配置

### 2. IoTDB集群部署

- **水平扩展**：支持IoTDB集群部署，提高写入和查询性能
- **数据分片**：通过合理的数据分片策略，分散负载
- **配置优化**：针对大规模时序数据场景优化IoTDB配置

### 3. Kafka高吞吐量支持

- **分区策略**：合理配置Kafka分区数，支持高并发消费
- **批量消费**：使用批量消费模式，提高数据处理效率
- **集群部署**：Kafka集群部署，提供高可用和高吞吐量

### 4. MySQL读写分离

- **主从复制**：配置MySQL主从复制，实现读写分离
- **连接池优化**：优化数据库连接池配置
- **批量操作**：使用批量插入和更新操作，提高性能

### 5. 缓存策略

- **热点数据缓存**：使用Redis缓存热点数据，减少数据库访问
- **分布式缓存**：Redis集群部署，提供高可用缓存服务

## 性能优化建议

1. **数据库优化**：
   - 合理设计索引
   - 使用连接池
   - 读写分离

2. **缓存策略**：
   - 使用Redis缓存热点数据
   - 合理设置缓存过期时间

3. **消息队列**：
   - 使用Kafka进行异步处理
   - 合理设置分区和副本

4. **微服务优化**：
   - 合理拆分服务边界
   - 使用熔断器防止雪崩
   - 负载均衡

5. **JVM调优**：
   - 根据服务特点调整堆内存大小
   - 选择合适的垃圾收集器

6. **容器化优化**：
   - 合理分配容器资源
   - 使用多阶段构建减小镜像大小

## 开发指南

1. 每个服务都是独立的Spring Boot应用
2. 服务间通过REST API或消息队列进行通信
3. 使用Nacos进行配置管理
4. 使用Spring Cloud Gateway作为统一入口
5. 使用JWT进行身份验证和授权

## 部署说明

支持多种部署方式：
1. 直接运行JAR包
2. 使用Docker容器化部署
3. 使用Kubernetes集群部署

## 贡献

欢迎提交Issue和Pull Request来改进这个项目。