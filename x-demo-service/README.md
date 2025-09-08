# X-Service 组件使用演示服务

本演示服务展示了项目中使用的所有中间件和技术组件如何协同工作。

## 技术组件演示

### 1. 数据库类中间件

#### MySQL
- 用于存储设备信息等结构化数据
- 通过JdbcTemplate进行数据操作
- 实现了设备的增删改查功能

#### MongoDB
- 用于存储设备操作日志等非结构化数据
- 记录设备的创建、更新、删除等操作日志

#### Redis
- 用于缓存设备信息，提高查询性能
- 实现了设备信息的缓存和失效机制

### 2. 消息队列类中间件

#### Kafka
- 用于设备数据的异步处理
- 设备数据通过Kafka进行流式处理

#### RabbitMQ
- 用于设备数据的异步处理
- 设备数据通过RabbitMQ进行消息传递

### 3. 对象存储

#### MinIO
- 用于存储设备数据文件
- 将设备数据以JSON格式存储到MinIO对象存储中

### 4. 网络通信框架

#### Netty
- 用于模拟设备接入和协议转换
- 启动Netty服务器监听设备连接
- 接收设备发送的数据并进行处理

### 5. 安全认证

#### JWT
- 用于用户身份认证和权限控制
- 实现了基于JWT的登录和权限验证机制

### 6. 监控和日志

#### Prometheus + Grafana
- 集成了Actuator监控端点
- 暴露/metrics端点供Prometheus采集

#### ELK
- 配置了结构化日志输出
- 支持日志收集、分析和可视化

## 功能演示

### 设备管理功能
1. 创建设备 - 将设备信息存储到MySQL，并同步到Redis缓存，同时记录到MongoDB日志
2. 查询设备 - 优先从Redis缓存获取，缓存未命中时查询MySQL
3. 更新设备状态 - 更新MySQL数据，清除Redis缓存，记录MongoDB日志
4. 删除设备 - 删除MySQL数据，清除Redis缓存，记录MongoDB日志

### 设备数据处理功能
1. 发送数据到Kafka - 将设备数据发送到Kafka进行流式处理
2. 发送数据到RabbitMQ - 将设备数据发送到RabbitMQ进行消息传递
3. 存储数据到MinIO - 将设备数据以JSON格式存储到MinIO对象存储
4. 异步处理数据 - 同时将设备数据发送到Kafka、RabbitMQ和MinIO

### 设备接入功能
1. Netty服务器 - 启动Netty服务器监听设备连接
2. 数据接收 - 接收设备发送的数据并进行处理
3. 数据分发 - 将接收到的设备数据异步分发到各个处理系统

### 安全认证功能
1. 用户登录 - 验证用户凭据并生成JWT token
2. Token验证 - 验证JWT token的有效性和权限
3. 权限控制 - 基于用户角色的接口访问控制

## API接口说明

### 认证相关接口
- `POST /api/login` - 用户登录，获取JWT token
- `POST /api/validate-token` - 验证JWT token

### 设备管理接口
- `POST /api/devices` - 创建设备（需要ADMIN权限）
- `GET /api/devices` - 获取所有设备（需要USER权限）
- `GET /api/devices/{id}` - 根据ID获取设备（需要USER权限）
- `PUT /api/devices/{id}/status` - 更新设备状态（需要ADMIN权限）
- `DELETE /api/devices/{id}` - 删除设备（需要ADMIN权限）

### 数据处理接口
- `POST /api/data/kafka` - 发送数据到Kafka（需要USER权限）
- `POST /api/data/rabbitmq` - 发送数据到RabbitMQ（需要USER权限）
- `POST /api/data/minio` - 存储数据到MinIO（需要USER权限）
- `POST /api/data/async` - 异步处理设备数据（需要USER权限）

## 部署说明

### 本地运行
1. 确保所有依赖的中间件服务已启动
2. 构建项目：`mvn clean package`
3. 运行应用：`java -jar target/x-demo-service.jar`

### Docker运行
1. 构建Docker镜像：`docker build -t demo-service .`
2. 运行容器：`docker run -p 8099:8099 -p 8001:8001 demo-service`

### Kubernetes部署
1. 应用部署配置：`kubectl apply -f k8s-deployment.yaml`

## 使用流程演示

1. 启动所有依赖的中间件服务（MySQL、Redis、MongoDB、Kafka、RabbitMQ、MinIO等）
2. 启动演示服务
3. 调用登录接口获取JWT token
4. 使用JWT token调用设备管理接口创建设备
5. 使用Netty客户端连接设备接入服务并发送数据
6. 观察数据如何通过Kafka、RabbitMQ和MinIO进行处理和存储
7. 通过监控端点查看服务运行状态

这个演示服务完整展示了物联网设备监控与管理系统中各个组件的协同工作方式，为实际项目开发提供了参考。