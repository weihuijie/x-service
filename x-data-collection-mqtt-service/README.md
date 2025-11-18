# MQTT Data Collection Service

MQTT数据采集服务，负责处理MQTT协议设备的接入和数据转发。

## 功能特性

- MQTT连接管理
- 设备数据订阅与接收
- 协议解析与数据格式转换
- 数据转发至后端服务

## 配置说明

在 `application.yml` 中进行相关配置：

- `mqtt.broker.url`: MQTT Broker地址
- `mqtt.broker.username`: 用户名（可选）
- `mqtt.broker.password`: 密码（可选）
- `mqtt.broker.client-id`: 客户端ID
- `mqtt.broker.default-topic`: 默认订阅主题
- `mqtt.broker.qos`: 服务质量等级
- `mqtt.broker.timeout`: 连接超时时间