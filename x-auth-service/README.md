# 认证服务 (x-auth-service)

## 项目概述

`x-auth-service` 是一个基于Spring Boot和gRPC实现的认证服务，负责用户认证、授权管理和Token验证等功能。

## 核心功能

1. **用户认证**：验证用户身份，生成JWT Token
2. **Token验证**：验证JWT Token的有效性
3. **权限检查**：检查用户是否具有特定权限
4. **gRPC服务**：提供高性能的gRPC接口供API网关调用

## 技术栈

- Spring Boot 2.6.x
- gRPC
- JWT (JSON Web Token)
- MySQL
- MyBatis

## 项目结构

```
src/main/java/com/x/auth/service/
├── AuthServiceApplication.java     # 应用入口
├── config/
│   └── GrpcConfig.java            # gRPC配置
├── controller/
│   └── AuthController.java        # RESTful接口控制器
├── grpc/
│   ├── AuthServiceImpl.java       # gRPC服务实现(基础)
│   └── AuthServiceImplExtended.java # gRPC服务实现(扩展)
├── util/
│   └── JwtUtil.java               # JWT工具类
```

## 配置说明

### 主要配置文件

- `src/main/resources/application.yml` - 核心配置文件

### 关键配置项

1. **服务器配置**
```yaml
server:
  port: 8087

# gRPC配置
grpc:
  server:
    port: 9092
```

2. **数据库配置**
```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/x_auth?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: root
    password: root
```

## gRPC接口

### 认证服务接口

1. **Login (登录)**
   - 方法：`rpc Login (LoginRequest) returns (LoginResponse);`
   - 功能：用户登录，验证凭据并生成Token

2. **ValidateToken (验证Token)**
   - 方法：`rpc ValidateToken (ValidateTokenRequest) returns (ValidateTokenResponse);`
   - 功能：验证Token的有效性

3. **CheckPermission (检查权限)**
   - 方法：`rpc CheckPermission (CheckPermissionRequest) returns (CheckPermissionResponse);`
   - 功能：检查用户是否具有特定权限

4. **ExecuteOperation (执行操作)**
   - 方法：`rpc ExecuteOperation (ExecuteOperationRequest) returns (ExecuteOperationResponse);`
   - 功能：通用操作执行接口，支持动态调用各种认证操作

## 使用方法

### 启动服务

```bash
# 使用Maven启动
mvn spring-boot:run

# 或使用打包后的jar文件
java -jar x-auth-service-1.0.0.jar
```

### 访问服务

- RESTful服务默认监听端口：8087
- gRPC服务默认监听端口：9092

## 注意事项

1. 确保MySQL数据库正常运行
2. 根据实际环境修改数据库连接配置
3. 确保gRPC端口未被占用
4. 在生产环境中，应加强密码安全策略

## 常见问题

1. **Q: 数据库连接失败怎么办？**
   A: 检查数据库服务是否正常运行，确认数据库连接配置是否正确。

2. **Q: gRPC服务启动失败怎么办？**
   A: 检查gRPC端口是否被占用，确认相关依赖是否正确引入。

3. **Q: Token验证失败怎么办？**
   A: 检查JWT密钥配置是否正确，确认Token是否过期。