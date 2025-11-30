# X-File-Service

基于MinIO的文件管理服务，用于X-Service物联网平台。

## 概述

该服务提供了使用MinIO作为后端存储的RESTful API文件操作。它支持上传、下载、删除和检索文件URL，并针对高并发场景进行了优化。

## 功能特性

- 文件上传与自动命名
- 文件下载
- 文件删除
- 生成预签名URL以临时访问
- 自动创建存储桶（如果不存在）
- 错误处理和全局异常管理
- 高并发支持
  - 线程池隔离：上传/下载用独立线程池，避免抢占业务线程资源，控制最大并发数
  - 分布式锁：基于Redis实现，防止同一文件重复上传，避免并发冲突
  - 分片上传：大文件拆分后并行上传，降低单文件上传压力，支持断点续传
  - 对象存储：用MinIO替代本地文件系统，支持分布式部署，适配高并发读写
  - 异步处理：文件操作异步执行，不阻塞前端请求，提升吞吐量
  - 流式操作：上传/下载均用流式处理，避免加载整个文件到内存，防止OOM
  - 唯一文件名：用UUID生成文件名，避免并发重名覆盖
  - 文件同步上传：支持将文件同步上传到多个目标位置

## 配置

服务通过[application.yml](src/main/resources/application.yml)进行配置:

```yaml
minio:
  endpoint: http://localhost:9000    # MinIO服务器端点
  access-key: minioadmin             # 访问密钥
  secret-key: minioadmin             # 秘密密钥
  bucket-name: files                 # 默认存储桶名称

spring:
  redis:
    host: localhost                  # Redis服务器主机
    port: 6379                       # Redis服务器端口
```

## API端点

### 上传文件

```
POST /api/files/upload
```

表单参数:
- `file`: 要上传的文件

响应:
```json
{
  "success": true,
  "message": "文件上传任务已提交"
}
```

### 查询上传结果

```
GET /api/files/upload/result/{fileId}
```

响应:
```json
{
  "success": true,
  "fileId": "文件ID",
  "status": "completed"
}
```

### 下载文件

```
GET /api/files/download/{fileName}
```

响应:
```json
{
  "success": true,
  "message": "文件下载任务已提交",
  "downloadUrl": "/api/files/download/result/{fileName}"
}
```

### 获取下载文件流

```
GET /api/files/download/result/{fileName}
```

直接返回文件流。

### 删除文件

```
DELETE /api/files/delete/{fileName}
```

从存储中删除指定的文件。

响应:
```json
{
  "success": true,
  "message": "文件删除成功"
}
```

### 获取文件URL

```
GET /api/files/url/{fileName}
```

获取用于临时访问文件的预签名URL。

响应:
```json
{
  "success": true,
  "fileUrl": "presigned-url-for-download"
}
```

### 初始化分片上传

```
POST /api/files/multipart/init/{fileName}
```

初始化分片上传。

响应:
```json
{
  "success": true,
  "uploadId": "上传ID"
}
```

### 上传文件分片

```
POST /api/files/multipart/upload
```

表单参数:
- `file`: 文件分片
- `fileName`: 文件名
- `partNumber`: 分片编号
- `uploadId`: 上传ID

响应:
```json
{
  "success": true,
  "chunkName": "分片名称"
}
```

### 完成分片上传

```
POST /api/files/multipart/complete
```

表单参数:
- `fileName`: 文件名
- `chunkNames`: 分片名称列表
- `uploadId`: 上传ID

响应:
```json
{
  "success": true,
  "message": "文件合并完成"
}
```

### 文件同步上传

```
POST /api/files/sync-upload
```

表单参数:
- `file`: 要上传的文件
- `targetUrls`: 目标URL数组

响应:
```json
{
  "success": true,
  "message": "文件同步上传任务已提交",
  "taskId": "任务ID"
}
```

### 查询同步上传任务状态

```
GET /api/files/sync-upload/status/{taskId}
```

响应:
```json
{
  "success": true,
  "taskId": "任务ID",
  "status": "completed",
  "progress": 100
}
```

## 接口调用示例

### 1. 基本文件上传

```bash
curl -X POST "http://localhost:8090/api/files/upload" \
     -H "accept: */*" \
     -H "Content-Type: multipart/form-data" \
     -F "file=@/path/to/your/file.txt"
```

### 2. 文件下载

```bash
# 首先发起下载请求
curl -X GET "http://localhost:8090/api/files/download/filename.txt"

# 然后获取文件流
curl -X GET "http://localhost:8090/api/files/download/result/filename.txt" \
     -o downloaded_file.txt
```

### 3. 分片上传

```bash
# 初始化分片上传
curl -X POST "http://localhost:8090/api/files/multipart/init/myfile.txt"

# 上传第一个分片
curl -X POST "http://localhost:8090/api/files/multipart/upload" \
     -H "Content-Type: multipart/form-data" \
     -F "file=@/path/to/chunk1.txt" \
     -F "fileName=myfile.txt" \
     -F "partNumber=1" \
     -F "uploadId=UPLOAD_ID_FROM_INIT"

# 上传第二个分片
curl -X POST "http://localhost:8090/api/files/multipart/upload" \
     -H "Content-Type: multipart/form-data" \
     -F "file=@/path/to/chunk2.txt" \
     -F "fileName=myfile.txt" \
     -F "partNumber=2" \
     -F "uploadId=UPLOAD_ID_FROM_INIT"

# 完成分片上传
curl -X POST "http://localhost:8090/api/files/multipart/complete" \
     -F "fileName=myfile.txt" \
     -F "chunkNames=chunk1_name_from_upload_response" \
     -F "chunkNames=chunk2_name_from_upload_response" \
     -F "uploadId=UPLOAD_ID_FROM_INIT"
```

### 4. 文件同步上传

```bash
curl -X POST "http://localhost:8090/api/files/sync-upload" \
     -H "Content-Type: multipart/form-data" \
     -F "file=@/path/to/your/file.txt" \
     -F "targetUrls=http://server1.example.com/upload" \
     -F "targetUrls=http://server2.example.com/upload" \
     -F "targetUrls=http://server3.example.com/upload"
```

### 5. 查询同步上传状态

```bash
curl -X GET "http://localhost:8090/api/files/sync-upload/status/TASK_ID"
```

## 实现详情

### 组件

1. **MinioConfig**: 配置并创建MinIO客户端bean
2. **FileStorageService**: 用于MinIO操作的核心服务
3. **FileController**: 公开文件操作的REST控制器
4. **GlobalExceptionHandler**: 统一处理异常
5. **ThreadPoolConfig**: 线程池配置
6. **DistributedLockService**: 分布式锁服务
7. **RedisConfig**: Redis配置

### 关键类

- [MinioConfig](src/main/java/com/x/file/service/config/MinioConfig.java) - MinIO客户端配置
- [FileStorageService](src/main/java/com/x/file/service/service/FileStorageService.java) - 文件操作服务
- [FileController](src/main/java/com/x/file/service/controller/FileController.java) - REST API控制器
- [GlobalExceptionHandler](src/main/java/com/x/file/service/exception/GlobalExceptionHandler.java) - 异常处理器
- [ThreadPoolConfig](src/main/java/com/x/file/service/config/ThreadPoolConfig.java) - 线程池配置
- [DistributedLockService](src/main/java/com/x/file/service/service/DistributedLockService.java) - 分布式锁服务
- [RedisConfig](src/main/java/com/x/file/service/config/RedisConfig.java) - Redis配置

## 使用方法

1. 确保MinIO服务器在配置的端点运行
2. 确保Redis服务器在配置的端点运行
3. 启动服务: `java -jar x-file-service.jar`
4. 如上所述使用REST API

## 错误处理

服务包含全面的错误处理:
- 文件大小超出限制
- 一般异常
- MinIO操作错误
- 分布式锁获取失败

错误以一致的格式返回:
```json
{
  "success": false,
  "error": "错误描述"
}
```