# 离线分析服务 (Offline Analysis Service)

## 简介

离线分析服务是一个基于 Apache Spark 和 Hadoop 生态系统的批处理分析平台，专门用于处理来自 IoTDB 的车联网时序数据。该服务提供了强大的数据清洗、聚合和分析功能，能够生成各种维度的报表，包括日报表和月报表。

## 功能特性

1. **IoTDB数据接入**：通过 Spark Connector 高效读取 IoTDB 中的时序数据
2. **数据清洗**：
   - 异常值过滤（GPS漂移、传感器超阈值等）
   - 缺失值填充
   - 故障码映射
   - 字段类型标准化

3. **数据分析聚合**：
   - 单车日汇总分析
   - 轨迹段分析（基于速度变化识别停车点）
   - 车队维度汇总

4. **MyBatis Plus集成**：
   - 使用MyBatis Plus进行数据库操作
   - 提供实体类映射
   - 支持分页查询

5. **报表生成**：
   - 日报表（车辆排名、油耗等指标）
   - 月报表（车队维度统计）

## 技术架构

- **计算引擎**：Apache Spark
- **存储系统**：Apache IoTDB, MySQL
- **开发语言**：Java 17
- **框架**：Spring Boot, MyBatis Plus

## 配置说明

主要配置项在 `application.yml` 文件中：

```yaml
server:
  port: 8089

spring:
  application:
    name: offline-analysis-service
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/vehicle_iot_result?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: root
    password: root
    
hadoop:
  namenode:
    ip: localhost
    port: 9000

iotdb:
  ip: localhost
  port: 6667

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
```

## API 接口

### 1. 执行批处理分析任务

```
POST /api/offline-analysis/batch-analysis?date={date}
```

参数：
- `date`（可选）：分析日期，格式为 yyyy-MM-dd，如果不提供则默认为前一天

### 2. 生成日报表

```
POST /api/offline-analysis/daily-report?date={date}
```

参数：
- `date`（可选）：报表日期，格式为 yyyy-MM-dd，如果不提供则默认为前一天

### 3. 生成月报表

```
POST /api/offline-analysis/monthly-report?month={month}
```

参数：
- `month`（可选）：报表月份，格式为 yyyy-MM，如果不提供则默认为上个月

## 部署方式

1. 构建项目：
   ```
   mvn clean package
   ```

2. 运行服务：
   ```
   java -jar target/x-offline-analysis-service.jar
   ```

或者使用 Docker：
```bash
docker build -t x-offline-analysis-service .
docker run -p 8089:8089 x-offline-analysis-service
```

## 数据处理流程

1. 从 IoTDB 读取原始时序数据（按日期过滤）
2. 数据清洗（过滤异常值、补全缺失值、故障码映射）
3. 聚合计算（单车日汇总、轨迹段汇总）
4. 通过 MyBatis Plus 将结果存储到 MySQL
5. 记录任务执行元数据

## 注意事项

1. 确保所有依赖的大数据组件（IoTDB、Hadoop、Spark等）正常运行
2. 根据实际环境调整配置文件中的连接参数
3. 在生产环境中建议使用 YARN 集群模式运行 Spark 作业