# Dubbo gRPC 使用说明

## 概述

本服务支持通过Dubbo gRPC进行服务调用，其他微服务可以通过Dubbo注解方式引用本服务提供的gRPC接口。

## 接口说明

设备服务提供了以下gRPC接口：

1. **CreateDevice** - 创建设备
2. **GetDevice** - 获取设备信息
3. **UpdateDeviceStatus** - 更新设备状态
4. **DeleteDevice** - 删除设备
5. **ListDevices** - 获取设备列表
6. **ExecuteOperation** - 执行通用操作

## 使用方法

### 1. 添加依赖

在调用方服务的pom.xml中添加以下依赖：

```xml
<dependency>
    <groupId>com.x</groupId>
    <artifactId>x-dubbo-grpc</artifactId>
    <version>1.0.0</version>
</dependency>

<dependency>
    <groupId>org.apache.dubbo</groupId>
    <artifactId>dubbo-spring-boot-starter</artifactId>
</dependency>
```

### 2. 配置Dubbo

在调用方服务的application.yml中添加Dubbo配置：

```yaml
dubbo:
  application:
    name: ${spring.application.name}
  registry:
    address: nacos://nacos:nacos@127.0.0.1:8848
```

### 3. 引用服务

在调用方的Service类中通过@DubboReference注解引用服务：

```java
@Service
public class DataService {
    
    @DubboReference(version = "1.0.0")
    private DeviceServiceGrpc.DeviceServiceBlockingStub deviceService;
    
    public void listDevices() {
        ListDevicesRequest request = ListDevicesRequest.newBuilder()
                .setPage(1)
                .setSize(10)
                .build();
        
        ListDevicesResponse response = deviceService.listDevices(request);
        // 处理响应
    }
}
```

## 注意事项

1. 确保Nacos服务正常运行
2. 确保服务提供方和调用方使用相同的注册中心地址
3. 注意服务版本号的匹配
4. 在生产环境中，应配置合适的超时时间和重试策略