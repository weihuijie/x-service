# API网关服务

## 项目概述

`x-api-gateway-service` 是一个基于Spring Cloud Gateway实现的API网关服务，作为微服务架构中的统一入口，负责请求路由、认证授权、限流熔断、日志监控等功能。

## 核心功能

1. **统一请求入口**：所有外部请求通过网关进入系统
2. **动态路由转发**：根据配置将请求转发到相应的微服务
3. **认证与授权**：统一的身份认证和权限校验
4. **请求限流**：基于Redis实现的请求限流，防止服务过载
5. **熔断降级**：当后端服务不可用或响应超时时提供降级处理
6. **负载均衡**：集成Spring Cloud LoadBalancer实现请求负载均衡
7. **请求/响应日志**：完整记录请求和响应信息，便于监控和调试
8. **文档集成**：集成Swagger/OpenAPI，提供API文档
9. **健康监控**：提供健康检查和监控端点
10. **服务追踪**：集成Prometheus监控指标导出

## 技术栈

- Spring Boot 2.6.x
- Spring Cloud Gateway 3.1.x
- Spring Cloud LoadBalancer
- Resilience4j (熔断器)
- Spring Data Redis Reactive (限流)
- Spring Cloud OpenFeign (服务间调用)
- Spring Boot Actuator (监控)
- SpringDoc OpenAPI (API文档)

## 项目结构

```
src/main/java/com/x/api/gateway/service/
├── ApiGatewayServiceApplication.java  # 应用入口
├── config/
│   ├── GatewayConfig.java             # 网关核心配置
│   └── RestTemplateConfig.java        # RestTemplate配置
├── filter/
│   ├── AuthFilter.java                # 认证过滤器
│   ├── LoggingFilter.java             # 日志过滤器
│   ├── RateLimiterFilter.java         # 限流过滤器
│   └── AddResponseHeaderGatewayFilterFactory.java # 自定义响应头过滤器
├── fallback/
│   └── FallbackController.java        # 熔断降级控制器
├── predicate/
│   └── QueryParamRoutePredicateFactory.java # 自定义请求参数断言
└── client/
    └── AuthServiceClient.java         # 认证服务Feign客户端
```

## 配置说明

### 主要配置文件

- `src/main/resources/application.yml` - 核心配置文件

### 关键配置项

1. **服务器配置**
```yaml
server:
  port: 9000
  servlet:
    context-path: /api-gateway
```

2. **路由配置**
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: manage-service
          uri: lb://x-manage-service
          predicates:
            - Path=/manage/**
          filters:
            - StripPrefix=1
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
                key-resolver: '#{@userKeyResolver}'
```

3. **限流配置**
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    database: 0
```

4. **熔断配置**
```yaml
resilience4j:
  circuitbreaker:
    instances:
      defaultCircuitBreaker:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 5000
```

## 使用方法

### 启动服务

```bash
# 使用Maven启动
mvn spring-boot:run

# 或使用打包后的jar文件
java -jar x-api-gateway-service-1.0.0.jar
```

### 访问服务

- 网关服务默认监听端口：9000
- 访问路径格式：`http://localhost:9000/api-gateway/{服务路由前缀}/{接口路径}`

例如：
- 访问管理服务：`http://localhost:9000/api-gateway/manage/users`
- 访问设备服务：`http://localhost:9000/api-gateway/device/list`

### API文档

Swagger UI访问地址：`http://localhost:9000/api-gateway/swagger-ui.html`

OpenAPI文档地址：`http://localhost:9000/api-gateway/v3/api-docs`

### 监控端点

Actuator监控地址：`http://localhost:9000/api-gateway/actuator`

网关特定监控：`http://localhost:9000/api-gateway/actuator/gateway/routes`

Prometheus监控指标：`http://localhost:9000/api-gateway/actuator/prometheus`

## 自定义扩展

### 添加新的路由

在`application.yml`的`spring.cloud.gateway.routes`节点下添加新的路由配置。

### 添加自定义过滤器

1. 创建实现`GlobalFilter`接口的过滤器类
2. 添加`@Component`注解使其被Spring容器管理
3. 实现`filter`方法和`getOrder`方法

### 添加自定义断言

1. 创建继承`AbstractRoutePredicateFactory`的断言工厂类
2. 添加`@Component`注解使其被Spring容器管理
3. 实现相关方法和配置类

## 注意事项

1. 确保Redis服务正常运行，限流功能依赖Redis
2. 在生产环境中，建议关闭或限制Swagger文档的访问
3. 根据实际需求调整限流参数和熔断参数
4. 白名单路径应根据实际情况进行配置
5. 认证服务`x-auth-service`需要正常运行，否则认证功能将无法正常工作
6. 确保所有后端微服务正常运行，以保证路由转发功能正常

## 常见问题

1. **Q: 路由转发失败怎么办？**
   A: 检查目标服务是否正常运行，查看网关日志确认路由配置是否正确。

2. **Q: 限流功能不生效怎么办？**
   A: 检查Redis连接是否正常，确认限流配置是否正确。

3. **Q: 服务熔断后如何恢复？**
   A: 熔断后，系统会在配置的时间后自动尝试恢复（半开状态），如果请求成功则完全恢复。

4. **Q: 如何添加新的认证白名单路径？**
   A: 在`AuthFilter`类的`WHITE_LIST`常量中添加新的路径前缀。

5. **Q: 认证服务调用超时怎么办？**
   A: 检查认证服务是否正常运行，适当调整认证服务调用的超时时间。