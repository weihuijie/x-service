package com.x.data.sync.service.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka 批量消费配置类
 */
@Configuration
public class KafkaBatchConsumerConfig {

    // Kafka基础配置
    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;
    @Value("${kafka.consumer.group-id:data-sync-group}")
    private String groupId;

    // 批量消费核心配置
    @Value("${kafka.consumer.batch-size:100}")
    private int batchSize;
    @Value("${kafka.consumer.fetch-min-size:10240}")
    private int fetchMinSize;
    @Value("${kafka.consumer.fetch-max-wait-ms:500}")
    private int fetchMaxWaitMs;
    @Value("${kafka.consumer.concurrency:3}")
    private int concurrency;

    /**
     * 批量消费容器工厂（独立Bean，供@KafkaListener引用）
     */
    @Bean(name = "batchKafkaListenerContainerFactory")
    public KafkaListenerContainerFactory<?> batchKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();

        // 配置Kafka消费者属性
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        // 批量消费关键配置
        consumerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, batchSize); // 每次poll最大消息数
        consumerProps.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, fetchMinSize); // 拉取最小字节数（触发批量条件）
        consumerProps.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, fetchMaxWaitMs); // 拉取最大等待时间（触发批量条件）

        // 生产级基础配置
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // 关闭自动提交，手动控制偏移量
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest"); // 偏移量重置策略
        consumerProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 10000); // 会话超时
        consumerProps.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 3000); // 心跳间隔

        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(consumerProps));
        factory.setConcurrency(concurrency); // 并发消费者数量（≤ 分区数）
        factory.setBatchListener(true); // 启用批量监听
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE); // 手动立即提交偏移量

        // （可选）配置重试和死信队列（生产环境建议开启）
        // factory.setErrorHandler(new SeekToCurrentErrorHandler(deadLetterPublishingRecoverer, new FixedBackOff(1000L, 3)));

        return factory;
    }
}