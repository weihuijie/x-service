package com.x.data.collection.service.utils.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka 生产者服务
 *
 * @author whj
 */
@Slf4j
@Component
public class KafkaProducerService {
    // 注入 Spring 自动配置的 KafkaTemplate
    private final KafkaTemplate<String, String> kafkaTemplate;

    // 主题名称（与消费者监听的主题一致）
    private static final String TOPIC_NAME = "test-topic";

    // 构造器注入
    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * 异步发送消息（带回调，推荐生产环境使用）
     * @param key 消息 Key
     * @param message 消息内容
     */
    public void sendMessageAsync(String key, String message) {
        // 发送消息（返回 CompletableFuture，支持回调）
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(TOPIC_NAME, key, message);

        // 回调处理发送结果
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                // 发送成功：打印主题、分区、偏移量等信息
                log.debug("【异步发送成功】主题：{}，分区：{}，偏移量：{}，Key：{}，消息：{}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        key,
                        message);
            } else {
                // 发送失败：打印异常信息
                System.err.printf("【异步发送失败】Key：%s，消息：%s，异常：%s%n",
                        key, message, ex.getMessage());
            }
        });
    }

    /**
     * 同步发送消息（阻塞等待结果，适用于需要立即获取发送状态的场景）
     * @param key 消息 Key
     * @param message 消息内容
     * @return 发送结果（包含元数据）
     * @throws Exception 发送异常
     */
    public SendResult<String, String> sendMessageSync(String key, String message) throws Exception {
        return kafkaTemplate.send(TOPIC_NAME, key, message).get(); // get() 阻塞等待
    }
}