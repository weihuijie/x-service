# 项目中间件统计

本文档统计了X-Service物联网设备监控与管理系统中使用的所有中间件和技术组件。

## 数据库类中间件

### 1. MySQL
- **用途**: 关系型数据库，用于存储结构化数据
- **使用模块**: 
  - x-manage-service
  - x-repository-service
  - x-auth-service
  - x-device-manage-service
  - x-alert-notice-service
  - x-offline-analysis-service
- **版本**: 8.1.0 (MySQL Connector/J)

### 2. MongoDB
- **用途**: 文档数据库，用于存储设备接入相关的非结构化数据
- **使用模块**: 
  - x-device-access-service
- **版本**: 4.8.0

### 3. IoTDB
- **用途**: 时序数据库，用于存储设备产生的时序数据
- **使用模块**: 
  - x-data-collection-service
  - x-data-sync-service
- **版本**: 0.13.3

## 缓存类中间件

### 4. Redis
- **用途**: 内存数据库，用于缓存和设备会话管理
- **使用模块**: 
  - x-device-access-service
  - x-realtime-analysis-service
- **用途**: 缓存热点数据、存储实时计算中间结果

## 消息队列类中间件

### 5. Kafka
- **用途**: 分布式流处理平台，用于设备数据的异步处理
- **使用模块**: 
  - x-data-collection-service
  - x-realtime-analysis-service
- **版本**: 3.3.1

### 6. RabbitMQ
- **用途**: 消息队列，用于告警通知和设备管理消息传递
- **使用模块**: 
  - x-device-manage-service
  - x-alert-notice-service
- **版本**: 5.16.0

## 微服务相关中间件

### 7. Nacos
- **用途**: 配置中心和服务注册发现
- **使用模块**: 
  - x-config-center-service
- **版本**: 2.2.0

### 8. Spring Cloud Gateway
- **用途**: API网关，统一接入与权限校验
- **使用模块**: 
  - x-api-gateway-service

### 9. OAuth2 + JWT
- **用途**: 用户认证和授权管理
- **使用模块**: 
  - x-auth-service

## 大数据处理框架

### 10. Hadoop
- **用途**: 分布式计算框架，用于离线批处理分析
- **使用模块**: 
  - x-offline-analysis-service
  - x-data-sync-service
- **版本**: 3.3.4

### 11. Flink
- **用途**: 流处理框架，用于实时数据分析
- **使用模块**: 
  - x-realtime-analysis-service
- **版本**: 1.16.1

## 网络通信框架

### 12. Netty
- **用途**: 高性能网络通信框架，用于设备接入和协议转换
- **使用模块**: 
  - x-device-access-service
- **版本**: 4.1.90.Final

## 任务调度框架

### 13. XXL-JOB
- **用途**: 分布式任务调度平台
- **使用模块**: 
  - x-task-scheduler-service
- **版本**: 2.3.1

## 对象存储

### 14. MinIO
- **用途**: 对象存储，用于管理设备固件和配置文件
- **使用模块**: 
  - x-file-service
- **版本**: 8.5.1

## 监控和日志组件

### 15. Prometheus
- **用途**: 监控和告警工具包
- **使用模块**: 所有服务模块

### 16. Grafana
- **用途**: 数据可视化平台
- **使用模块**: 用于展示监控数据

### 17. ELK Stack (Elasticsearch, Logstash, Kibana)
- **用途**: 日志收集、分析和可视化
- **使用模块**: 所有服务模块

## 总结

本项目共使用了17种主要的中间件和技术组件，涵盖了数据存储、缓存、消息传递、微服务、大数据处理、网络通信、任务调度、对象存储以及监控日志等多个领域，构建了一个完整且功能丰富的物联网设备监控与管理系统。