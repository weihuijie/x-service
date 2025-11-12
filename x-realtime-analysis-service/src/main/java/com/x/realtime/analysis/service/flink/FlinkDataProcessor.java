package com.x.realtime.analysis.service.flink;

import com.alibaba.fastjson2.JSONObject;
import com.x.realtime.analysis.service.rabbitmq.CustomRabbitMQSink;
import com.x.realtime.analysis.service.rabbitmq.RabbitMQConfigProperties;
import com.x.repository.service.entity.DeviceInfoEntity;
import com.x.repository.service.entity.DevicePointInfoEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
/**
 * Flink数据处理类
 *
 * @author whj
 */
@Slf4j
@Component
public class FlinkDataProcessor {

    private final RabbitMQConfigProperties rabbitMQConfig;

    // 定义阈值，用于检测异常数据
    private static final double TEMPERATURE_THRESHOLD = 99.0;
    private static final double PRESSURE_THRESHOLD = 100.0;

    public FlinkDataProcessor(RabbitMQConfigProperties rabbitMQConfig) {
        this.rabbitMQConfig = rabbitMQConfig;
    }

    public void processKafkaStream() {
        try {
            // 创建Flink执行环境
            final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
            env.setParallelism(3);

            // 启用Checkpoint（生产环境必加）
            env.enableCheckpointing(5000);
            CheckpointConfig checkpointConfig = env.getCheckpointConfig();
            checkpointConfig.setCheckpointingMode(CheckpointingMode.AT_LEAST_ONCE);
            checkpointConfig.setExternalizedCheckpointCleanup(
                    CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION
            ); // 取消作业时保留 Checkpoint（便于恢复偏移量）

            // Kafka消费者配置
            KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                    .setBootstrapServers("127.0.0.1:9092")
                    .setTopics("test-topic")
                    .setGroupId("data-realtime-group")
                    .setClientIdPrefix("data-collection-consumer-analysis")
                    .setStartingOffsets(OffsetsInitializer.latest())
                    .setValueOnlyDeserializer(new SimpleStringSchema())
                    // 新增：配置 Kafka 消费者参数，解决重平衡问题
                    .setProperty("max.poll.interval.ms", "600000") // 10 分钟（单位：ms）
                    .setProperty("max.poll.records", "100") // 单次拉取 100 条（根据数据大小调整）
                    .setProperty("heartbeat.interval.ms", "3000") // 心跳间隔（默认 3000ms，可选优化）
                    .setProperty("session.timeout.ms", "60000") // 会话超时 60 秒（默认 45 秒，延长避免误判）
                    .setProperty("rebalance.timeout.ms", "300000") // 重平衡超时 5 分钟（默认 5 分钟，确保重平衡完成）
                    .setProperty("partition.assignment.strategy", "org.apache.kafka.clients.consumer.RoundRobinAssignor") // 轮询分配策略（适配多分区）
                    .setProperty("enable.auto.commit", "false") // 禁用 Kafka 自动提交
                    .setStartingOffsets(OffsetsInitializer.latest())
                    .build();

            // 添加数据源
            DataStream<String> inputStream = env.fromSource(
                    kafkaSource,
                    WatermarkStrategy.noWatermarks(), // 字符串层面无需Watermark，用无水印策略
                    "Kafka Source"
            );

            // 解析并处理数据
            DataStream<DevicePointInfoEntity> sensorDataStream = inputStream
                    // 解析JSON字符串为List<DevicePointInfoEntity>
                    .map(new DataParser())
                    .filter(Objects::nonNull) // 过滤解析失败的null List
                    .filter(list -> !list.isEmpty()) // 过滤空List
                    // FlatMap拆分+显式声明输出类型（解决类型推断问题！）
                    .flatMap(new ListFlattenFunction())
                    .returns(DevicePointInfoEntity.class) // 强制Flink识别输出为DevicePointInfoEntity
                    // 过滤无效DevicePointInfoEntity（修复隐藏bug1：过滤null+pointValue为空）
                    .filter(sensorData -> sensorData != null && sensorData.getPointValue() != null)
                    // 只配置一次Watermark（针对DevicePointInfoEntity的时间戳）
                    .assignTimestampsAndWatermarks(
                            WatermarkStrategy.<DevicePointInfoEntity>forBoundedOutOfOrderness(Duration.ofSeconds(5))
                                    .withTimestampAssigner((data, recordTimestamp) -> System.currentTimeMillis())
                    );

            // 检测温度异常
            DataStream<DevicePointInfoEntity> temperatureAlerts = sensorDataStream
                    .filter(new TemperatureAlertFilter());

            // 检测压力异常
            DataStream<DevicePointInfoEntity> pressureAlerts = sensorDataStream
                    .filter(new PressureAlertFilter());

            // 合并所有告警
            DataStream<DevicePointInfoEntity> allAlerts = temperatureAlerts.union(pressureAlerts);

            // 发送到RabbitMQ
            allAlerts.addSink(new CustomRabbitMQSink(rabbitMQConfig))
                    .name("Custom-RabbitMQ-Sink")
                    .uid("rabbitmq-sink-uid");

            // 调试打印
            allAlerts.print();

            // 执行作业
            env.execute("Sensor Data Analysis Job");
        } catch (Exception e) {
            log.error("Flink job execution failed", e); // 用log代替e.printStackTrace()
        }
    }

    // 数据解析函数
    public static class DataParser implements MapFunction<String, List<DevicePointInfoEntity>> {
        @Override
        public List<DevicePointInfoEntity> map(String value) {
            try {
                log.debug("Received sensor data: {}", value);
                // FastJSON解析JSON数组为List<DevicePointInfoEntity>（确保JSON格式正确）
                DeviceInfoEntity deviceInfoEntity = JSONObject.parseObject(value, DeviceInfoEntity.class);
                return deviceInfoEntity.getPointList();
            } catch (Exception e) {
                log.error("Failed to parse sensor data: {}", value, e); // 用log记录错误，便于排查
                return null;
            }
        }
    }

    // List拆分函数
    public static class ListFlattenFunction implements FlatMapFunction<List<DevicePointInfoEntity>, DevicePointInfoEntity> {
        @Override
        public void flatMap(List<DevicePointInfoEntity> sensorDataList, Collector<DevicePointInfoEntity> collector) {
            for (DevicePointInfoEntity sensorData : sensorDataList) {
                if (sensorData != null) {
                    collector.collect(sensorData);
                }
            }
        }
    }

    // 温度异常检测
    public static class TemperatureAlertFilter implements FilterFunction<DevicePointInfoEntity> {
        @Override
        public boolean filter(DevicePointInfoEntity value) {
            // 先判断pointValue非空，避免空指针
            if (value.getPointValue() == null) {
                return false;
            }
            // 安全转换数值
            try {
                double valueDouble = new BigDecimal(value.getPointValue().toString()).doubleValue();
                return valueDouble > TEMPERATURE_THRESHOLD;
            } catch (Exception e) {
                log.warn("Failed to parse temperature value: {}", value.getPointValue());
                return false;
            }
        }
    }

    // 压力异常检测
    public static class PressureAlertFilter implements FilterFunction<DevicePointInfoEntity> {
        @Override
        public boolean filter(DevicePointInfoEntity value) {
            if (value.getPointValue() == null) {
                return false;
            }
            try {
                double valueDouble = new BigDecimal(value.getPointValue().toString()).doubleValue();
                return valueDouble > PRESSURE_THRESHOLD;
            } catch (Exception e) {
                log.warn("Failed to parse pressure value: {}", value.getPointValue());
                return false;
            }
        }
    }
}