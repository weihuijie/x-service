package com.x.realtime.analysis.service.flink;

import com.alibaba.fastjson2.JSONArray;
import com.x.realtime.analysis.service.rabbitmq.CustomRabbitMQSink;
import com.x.realtime.analysis.service.rabbitmq.RabbitMQConfigProperties;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class FlinkDataProcessor {

    @Autowired
    private RabbitMQConfigProperties rabbitMQConfig;

    // 定义阈值，用于检测异常数据
    private static final double TEMPERATURE_THRESHOLD = 80.0;
    private static final double PRESSURE_THRESHOLD = 100.0;

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
                    .setBootstrapServers("localhost:9092")
                    .setTopics("test-topic")
                    .setGroupId("data-collection-group")
                    .setStartingOffsets(OffsetsInitializer.earliest())
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

            // 添加数据源（删除重复的Watermark配置！）
            DataStream<String> inputStream = env.fromSource(
                    kafkaSource,
                    WatermarkStrategy.noWatermarks(), // 字符串层面无需Watermark，用无水印策略
                    "Kafka Source"
            );

            // 解析并处理数据（核心修复在这里！）
            DataStream<SensorData> sensorDataStream = inputStream
                    // 第一步：解析JSON字符串为List<SensorData>
                    .map(new DataParser())
                    .filter(Objects::nonNull) // 过滤解析失败的null List
                    .filter(list -> !list.isEmpty()) // 过滤空List
                    // 第二步：FlatMap拆分+显式声明输出类型（解决类型推断问题！）
                    .flatMap(new ListFlattenFunction())
                    .returns(SensorData.class) // 关键修复：强制Flink识别输出为SensorData
                    // 第三步：过滤无效SensorData（修复隐藏bug1：过滤null+pointValue为空）
                    .filter(sensorData -> sensorData != null && sensorData.getPointValue() != null)
                    // 第四步：只配置一次Watermark（针对SensorData的时间戳）
                    .assignTimestampsAndWatermarks(
                            WatermarkStrategy.<SensorData>forBoundedOutOfOrderness(Duration.ofSeconds(5))
                                    .withTimestampAssigner((data, recordTimestamp) -> System.currentTimeMillis())
                    );

            // 检测温度异常（修复隐藏bug2：判断pointValue非空）
            DataStream<SensorData> temperatureAlerts = sensorDataStream
                    .filter(new TemperatureAlertFilter());

            // 检测压力异常（同理修复）
            DataStream<SensorData> pressureAlerts = sensorDataStream
                    .filter(new PressureAlertFilter());

            // 合并所有告警
            DataStream<SensorData> allAlerts = temperatureAlerts.union(pressureAlerts);

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

    // 数据解析函数（无需修改，保持原样）
    public static class DataParser implements MapFunction<String, List<SensorData>> {
        @Override
        public List<SensorData> map(String value) {
            try {
                log.info("Received sensor data: {}", value);
                // FastJSON解析JSON数组为List<SensorData>（确保JSON格式正确）
                return JSONArray.parseArray(value, SensorData.class);
            } catch (Exception e) {
                log.error("Failed to parse sensor data: {}", value, e); // 用log记录错误，便于排查
                return null;
            }
        }
    }

    // List拆分函数（无需修改，保持原样）
    public static class ListFlattenFunction implements FlatMapFunction<List<SensorData>, SensorData> {
        @Override
        public void flatMap(List<SensorData> sensorDataList, Collector<SensorData> collector) {
            for (SensorData sensorData : sensorDataList) {
                if (sensorData != null) {
                    collector.collect(sensorData);
                }
            }
        }
    }

    // 温度异常检测（修复空指针：先判断pointValue非空）
    public static class TemperatureAlertFilter implements FilterFunction<SensorData> {
        @Override
        public boolean filter(SensorData value) {
            // 先判断pointValue非空，避免空指针
            if (value.getPointValue() == null) {
                return false;
            }
            // 安全转换数值（处理不同类型的pointValue：int/double/bool）
            try {
                double valueDouble = new BigDecimal(value.getPointValue().toString()).doubleValue();
                return valueDouble > TEMPERATURE_THRESHOLD;
            } catch (Exception e) {
                log.warn("Failed to parse temperature value: {}", value.getPointValue());
                return false;
            }
        }
    }

    // 压力异常检测（同理修复）
    public static class PressureAlertFilter implements FilterFunction<SensorData> {
        @Override
        public boolean filter(SensorData value) {
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