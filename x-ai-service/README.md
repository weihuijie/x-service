# x-ai-service

AI服务模块，提供人工智能相关功能。

## 功能特性

- 集成Spring AI框架
- 支持多种大语言模型（默认集成OpenAI）
- AI模型管理
- 推理服务
- 数据处理和特征工程

## 端口配置

- HTTP端口: 8090
- gRPC端口: 9095

## 使用说明

### 配置
在使用之前，需要配置相应的AI服务密钥：
- OpenAI: 设置环境变量 `OPENAI_API_KEY`

### API接口
- GET /ai/chat?message=你好 -> 基础对话
- GET /ai/poem?topic=春天 -> 生成诗歌

### 示例
```bash
curl "http://localhost:8090/ai/chat?message=你好，Spring AI!"
curl "http://localhost:8090/ai/poem?topic=春天"
```