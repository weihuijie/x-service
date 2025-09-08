# X-Service 组件工具类使用说明

本文档详细说明了项目中为各种中间件组件创建的工具类及其使用方法。

## 1. 概述

为了简化项目中各种中间件组件的使用，我们在 [x-common](file:///D:/work/project/me/x-service/x-common) 模块中创建了统一的组件工具类。这些工具类封装了各组件的常用操作，提供了简洁的API接口，隐藏了底层实现细节，便于在项目中统一使用。

## 2. 工具类列表

### 2.1 Redis 组件工具类
**类名**: [RedisComponentUtil](file:///D:/work/project/me/x-service/x-common/src/main/java/com/x/common/component/RedisComponentUtil.java)
**包路径**: com.x.common.component

封装了Redis的常用操作：
- 设置字符串类型的键值对
- 获取字符串类型的值
- 删除指定键
- 判断指定键是否存在
- 设置过期时间

### 2.2 Kafka 组件工具类
**类名**: [KafkaComponentUtil](file:///D:/work/project/me/x-service/x-common/src/main/java/com/x/common/component/KafkaComponentUtil.java)
**包路径**: com.x.common.component

封装了Kafka消息发送的常用操作：
- 发送消息到指定主题
- 发送消息到指定主题和分区
- 发送消息到指定主题，指定键和值

### 2.3 RabbitMQ 组件工具类
**类名**: [RabbitMQComponentUtil](file:///D:/work/project/me/x-service/x-common/src/main/java/com/x/common/component/RabbitMQComponentUtil.java)
**包路径**: com.x.common.component

封装了RabbitMQ消息发送的常用操作：
- 发送消息到指定交换机和路由键
- 发送消息到指定队列
- 发送消息并设置消息ID

### 2.4 MongoDB 组件工具类
**类名**: [MongoDBComponentUtil](file:///D:/work/project/me/x-service/x-common/src/main/java/com/x/common/component/MongoDBComponentUtil.java)
**包路径**: com.x.common.component

封装了MongoDB的常用操作：
- 获取数据库和集合
- 插入文档（单个和多个）
- 查询文档（条件查询和全量查询）
- 更新文档（单个和多个）
- 删除文档（单个和多个）

### 2.5 MinIO 组件工具类
**类名**: [MinIOComponentUtil](file:///D:/work/project/me/x-service/x-common/src/main/java/com/x/common/component/MinIOComponentUtil.java)
**包路径**: com.x.common.component

封装了MinIO对象存储的常用操作：
- 检查存储桶是否存在
- 创建存储桶
- 上传对象（流和字节数组）
- 下载对象
- 删除对象
- 列出存储桶中的所有对象
- 获取对象URL

### 2.6 JWT 组件工具类
**类名**: [JwtComponentUtil](file:///D:/work/project/me/x-service/x-common/src/main/java/com/x/common/component/JwtComponentUtil.java)
**包路径**: com.x.common.component

封装了JWT Token的常用操作：
- 生成JWT Token（普通和带角色信息）
- 解析JWT Token获取声明
- 从JWT Token中获取用户名和角色
- 验证JWT Token是否过期或有效

### 2.7 gRPC 组件工具类
**类名**: [GrpcComponentUtil](file:///D:/work/project/me/x-service/x-common/src/main/java/com/x/common/component/GrpcComponentUtil.java)
**包路径**: com.x.common.component

封装了gRPC客户端的常用操作：
- 安全关闭gRPC通道
- 检查gRPC通道是否可用
- 处理gRPC调用异常

### 2.8 Netty 组件工具类
**类名**: [NettyComponentUtil](file:///D:/work/project/me/x-service/x-common/src/main/java/com/x/common/component/NettyComponentUtil.java)
**包路径**: com.x.common.component

封装了Netty网络通信的常用操作：
- 发送字符串消息到通道
- 发送字节消息到通道
- 关闭通道
- 获取远程地址和本地地址
- 检查通道是否活跃

### 2.9 MySQL 组件工具类
**类名**: [MySQLComponentUtil](file:///D:/work/project/me/x-service/x-common/src/main/java/com/x/common/component/MySQLComponentUtil.java)
**包路径**: com.x.common.component

封装了MySQL数据库操作的常用方法：
- 执行查询SQL，返回单个结果或结果列表
- 执行更新SQL（INSERT、UPDATE、DELETE）
- 执行插入SQL并返回自增主键
- 执行查询SQL，返回单个值
- 检查是否存在满足条件的记录

### 2.10 IoTDB 组件工具类
**类名**: [IoTDBComponentUtil](file:///D:/work/project/me/x-service/x-common/src/main/java/com/x/common/component/IoTDBComponentUtil.java)
**包路径**: com.x.common.component

封装了IoTDB时序数据库操作的常用方法：
- 插入单条时序数据
- 批量插入时序数据
- 执行非查询SQL

### 2.11 Hadoop 组件工具类
**类名**: [HadoopComponentUtil](file:///D:/work/project/me/x-service/x-common/src/main/java/com/x/common/component/HadoopComponentUtil.java)
**包路径**: com.x.common.component

封装了Hadoop HDFS操作的常用方法：
- 连接到HDFS
- 上传文件到HDFS
- 从HDFS下载文件
- 在HDFS上创建目录
- 检查HDFS上文件或目录是否存在
- 删除HDFS上的文件或目录
- 列出HDFS目录下的文件和子目录

### 2.12 Flink 组件工具类
**类名**: [FlinkComponentUtil](file:///D:/work/project/me/x-service/x-common/src/main/java/com/x/common/component/FlinkComponentUtil.java)
**包路径**: com.x.common.component

封装了Flink流处理操作的常用方法：
- 创建流执行环境
- 对数据流进行映射转换
- 对数据流进行过滤
- 执行流处理任务

### 2.13 XXL-JOB 组件工具类
**类名**: [XxlJobComponentUtil](file:///D:/work/project/me/x-service/x-common/src/main/java/com/x/common/component/XxlJobComponentUtil.java)
**包路径**: com.x.common.component

封装了XXL-JOB任务调度操作的常用方法：
- 获取任务参数
- 记录任务日志
- 设置任务处理成功或失败
- 获取当前任务ID、分片索引和分片总数

### 2.14 Nacos 组件工具类
**类名**: [NacosComponentUtil](file:///D:/work/project/me/x-service/x-common/src/main/java/com/x/common/component/NacosComponentUtil.java)
**包路径**: com.x.common.component

封装了Nacos配置中心和服务注册发现操作的常用方法：
- 获取配置信息
- 发布配置信息
- 删除配置信息
- 注册服务实例
- 获取服务实例列表
- 注销服务实例

### 2.15 Elasticsearch 组件工具类
**类名**: [ElasticsearchComponentUtil](file:///D:/work/project/me/x-service/x-common/src/main/java/com/x/common/component/ElasticsearchComponentUtil.java)
**包路径**: com.x.common.component

封装了Elasticsearch搜索引擎操作的常用方法：
- 索引文档
- 搜索文档
- 搜索所有文档
- 检查Elasticsearch客户端是否连接正常

### 2.16 Prometheus 组件工具类
**类名**: [PrometheusComponentUtil](file:///D:/work/project/me/x-service/x-common/src/main/java/com/x/common/component/PrometheusComponentUtil.java)
**包路径**: com.x.common.component

封装了Prometheus监控指标操作的常用方法：
- 创建并增加计数器
- 记录方法执行时间
- 记录耗时

## 3. 使用方法

### 3.1 引入依赖
确保需要使用组件工具类的模块已经在pom.xml中添加了[x-common](file:///D:/work/project/me/x-service/x-common)依赖：

```xml
<dependency>
    <groupId>com.x</groupId>
    <artifactId>x-common</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 3.2 注入工具类
在需要使用的类中通过@Autowired注解注入相应的工具类：

```java
@Service
public class ExampleService {
    
    @Autowired
    private RedisComponentUtil redisComponentUtil;
    
    @Autowired
    private KafkaComponentUtil kafkaComponentUtil;
    
    // 其他业务代码
}
```

### 3.3 调用方法
通过注入的工具类实例调用相应的方法：

```java
// 使用Redis工具类
redisComponentUtil.setString("key", "value");
String value = (String) redisComponentUtil.getString("key");

// 使用Kafka工具类
kafkaComponentUtil.sendMessage("topic", "message");
```

## 4. 注意事项

1. 所有组件工具类都使用了Spring的@Component注解，因此可以直接通过@Autowired注入使用。
2. 部分工具类依赖于相应的客户端配置，需要在application.yml中正确配置相关组件的连接信息。
3. 使用工具类时应注意异常处理，特别是在网络通信和IO操作中。
4. 工具类中的方法尽可能保持简洁，复杂的业务逻辑应在服务层实现。