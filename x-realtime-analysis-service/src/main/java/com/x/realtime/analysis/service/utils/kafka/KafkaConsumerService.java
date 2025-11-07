//package com.x.realtime.analysis.service.utils.kafka;
//
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Component;
//
///**
// * Kafka 消费者服务（监听 test-topic 主题）
// */
//@Component
//public class KafkaConsumerService {
//
//    /**
//     * 监听 test-topic 主题（注解驱动，自动消费）
//     * @param record 消息记录（包含 Key、Value、元数据等）
//     */
//    @KafkaListener(topics = "test-topic", groupId = "data-collection-group")
//    public void consumeMessage(ConsumerRecord<String, String> record) {
//        // 解析消息内容
//        String key = record.key();
//        String value = record.value();
//        String topic = record.topic();
//        int partition = record.partition();
//        long offset = record.offset();
//
//        // 业务处理（此处仅打印，实际项目可替换为数据库存储、业务逻辑处理等）
//        System.out.printf("【消费者接收成功】消费者组：data-collection-group，主题：%s，分区：%d，偏移量：%d，Key：%s，消息：%s%n",
//                topic, partition, offset, key, value);
//    }
//}