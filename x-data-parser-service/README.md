# x-data-parser-service 数据解析服务

## 介绍

x-data-parser-service 是专门用于处理各种数据解析的服务模块，负责将原始数据转换为统一格式供后续处理使用。

## 功能特性

- 支持多种数据格式解析
- 提供统一的数据输出格式
- 高性能并发处理能力
- 集成 Kafka 消息队列
- 支持动态配置更新

## 端口

默认端口: 9207

## 配置说明

主要配置项：
- server.port: 服务端口
- spring.application.name: 服务注册名称
- spring.cloud.nacos: Nacos 配置中心和服务发现地址
- spring.kafka: Kafka 消息队列配置

## 启动方式

```bash
mvn clean package
java -jar target/x-data-parser-service.jar
```

或使用 Docker 启动:

```bash
docker build -t x-data-parser-service .
docker run -p 9207:9207 x-data-parser-service
```