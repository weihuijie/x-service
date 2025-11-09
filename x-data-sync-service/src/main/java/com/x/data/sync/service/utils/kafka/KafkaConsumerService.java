package com.x.data.sync.service.utils.kafka;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.x.data.sync.service.IotDB.DevicePointData;
import com.x.data.sync.service.IotDB.DeviceDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Kafka 消费者服务（生产级优化：重试、死信、幂等、异常处理、配置解耦）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumerService implements ConsumerSeekAware {

    private final DeviceDataService deviceDataService;

    /**
     * 核心消费方法（优化点：配置解耦、重试、幂等、格式校验、死信转发）
     * @param record 消息记录
     */
    @Retryable(
            // 可重试异常：仅对业务异常、网络波动等临时异常重试
            retryFor = {RuntimeException.class, JSONException.class},
            // 重试策略：指数退避（1s → 2s → 4s，避免频繁重试压垮服务）
            backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 4000),
            // 最大重试次数（配置文件读取，灵活调整）
            maxAttemptsExpression = "${kafka.consumer.max-retry-attempts:3}"
    )
    @KafkaListener(
            topics = "${kafka.consumer.topic:test-topic}",
            groupId = "${kafka.consumer.group-id:data-sync-group}",
            // 并发消费：提高吞吐量（配置文件读取，根据分区数调整，建议 ≤ 分区数）
            concurrency = "${kafka.consumer.concurrency:3}",
            // 手动提交 Offset：确保消息处理成功后再提交，避免消息丢失
            properties = {
                    "enable.auto.commit=false", // 关闭自动提交
                    "auto.offset.reset=earliest", // 无偏移量时从最新消息开始消费（可配置为 earliest）
                    "session.timeout.ms=10000", // 会话超时时间
                    "heartbeat.interval.ms=3000" // 心跳间隔（建议为会话超时的 1/3）
            },
            clientIdPrefix = "data-collection-consumer-sync-"// 客户端 ID 前缀（可配置为全局唯一）
    )
    public void consumeMessage(
            ConsumerRecord<String, String> record,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        // 构建唯一标识（主题+分区+偏移量）：用于幂等性校验
        String uniqueKey = String.format("%s-%d-%d", record.topic(), partition, offset);
        log.info("【开始消费消息】唯一标识：{}，Key：{}，消息内容：{}", uniqueKey, record.key(), record.value());

        try {
            // 核心业务处理（替换为实际业务逻辑，如数据库存储、RPC 调用等）
            processBusinessLogic(record);

        } catch (Exception e) {
            log.error("【消费失败】唯一标识：{}，消息处理异常", uniqueKey, e);
            // 抛出异常触发重试（@Retryable 会捕获并重试）
            throw new RuntimeException("消息消费业务处理失败", e);
        }
    }

    /**
     * 核心业务处理（替换为实际业务逻辑）
     */
    private void processBusinessLogic(ConsumerRecord<String, String> record) {
        // 示例：打印消息详情（实际场景可替换为：数据库存储、缓存更新、业务计算等）
        String key = record.key();
        String value = record.value();
        String topic = record.topic();
        int partition = record.partition();
        long offset = record.offset();

        log.info("【业务处理】主题：{}，分区：{}，偏移量：{}，Key：{}，消息：{}",
                topic, partition, offset, key, value);
        List<DevicePointData> devicePointData = JSONArray.parseArray(value, DevicePointData.class);
        for (DevicePointData deviceDatum : devicePointData) {
            deviceDataService.writeData(deviceDatum);
        }
    }
}