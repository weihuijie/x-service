# 设备接入服务 REST API 文档

## 概述

设备接入服务除了提供gRPC接口外，还提供了一套REST API接口，方便通过HTTP协议访问设备管理功能。

## API列表

### 1. 创建设备

**请求URL**: `POST /device`  
**功能**: 创建一个新的设备

**请求体**:
```json
{
  "name": "设备名称",
  "model": "设备型号",
  "status": "设备状态",
  "location": "设备位置"
}
```

**响应**:
```json
{
  "success": true,
  "message": "设备创建成功",
  "device": {
    "id": 1,
    "name": "设备名称",
    "model": "设备型号",
    "status": "设备状态",
    "location": "设备位置",
    "createTime": 1234567890,
    "updateTime": 1234567890
  }
}
```

### 2. 获取设备信息

**请求URL**: `GET /device/{id}`  
**功能**: 根据设备ID获取设备信息

**响应**:
```json
{
  "success": true,
  "message": "获取设备成功",
  "device": {
    "id": 1,
    "name": "设备名称",
    "model": "设备型号",
    "status": "设备状态",
    "location": "设备位置",
    "createTime": 1234567890,
    "updateTime": 1234567890
  }
}
```

### 3. 更新设备状态

**请求URL**: `PUT /device/{id}/status`  
**功能**: 更新设备状态

**请求体**:
```json
{
  "status": "新状态"
}
```

**响应**:
```json
{
  "success": true,
  "message": "设备状态更新成功",
  "device": {
    "id": 1,
    "name": "设备名称",
    "model": "设备型号",
    "status": "新状态",
    "location": "设备位置",
    "createTime": 1234567890,
    "updateTime": 1234567890
  }
}
```

### 4. 删除设备

**请求URL**: `DELETE /device/{id}`  
**功能**: 删除指定ID的设备

**响应**:
```json
{
  "success": true,
  "message": "设备删除成功"
}
```

### 5. 获取设备列表

**请求URL**: `GET /device/list?page=1&size=10&status=online`  
**功能**: 获取设备列表，支持分页和状态筛选

**参数**:
- `page`: 页码，默认为1
- `size`: 每页大小，默认为10
- `status`: 设备状态筛选条件，可选

**响应**:
```json
{
  "success": true,
  "message": "获取设备列表成功",
  "devices": [
    {
      "id": 1,
      "name": "设备名称",
      "model": "设备型号",
      "status": "设备状态",
      "location": "设备位置",
      "createTime": 1234567890,
      "updateTime": 1234567890
    }
  ],
  "total": 100
}
```

### 6. 执行操作

**请求URL**: `POST /device/execute`  
**功能**: 通用操作执行接口

**请求体**:
```json
{
  "operation": "create",
  "params": {
    "name": "设备名称",
    "model": "设备型号",
    "status": "设备状态",
    "location": "设备位置"
  }
}
```

**响应**:
```json
{
  "success": true,
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": "1",
    "name": "设备名称",
    "model": "设备型号",
    "status": "设备状态",
    "location": "设备位置"
  }
}
```

## 使用示例

### 创建设备
```bash
curl -X POST http://localhost:8083/device \
  -H "Content-Type: application/json" \
  -d '{
    "name": "温度传感器001",
    "model": "TS-100",
    "status": "online",
    "location": "A区-01"
  }'
```

### 获取设备信息
```bash
curl -X GET http://localhost:8083/device/1
```

### 更新设备状态
```bash
curl -X PUT http://localhost:8083/device/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "offline"}'
```

### 删除设备
```bash
curl -X DELETE http://localhost:8083/device/1
```

### 获取设备列表
```bash
curl -X GET "http://localhost:8083/device/list?page=1&size=10&status=online"
```

### 执行操作
```bash
curl -X POST http://localhost:8083/device/execute \
  -H "Content-Type: application/json" \
  -d '{
    "operation": "list",
    "params": {
      "page": "1",
      "size": "10"
    }
  }'
```