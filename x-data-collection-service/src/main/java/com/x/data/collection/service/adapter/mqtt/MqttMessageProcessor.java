//package com.x.data.collection.service.adapter.mqtt;
//
//import com.x.data.collection.service.adapter.validation.DeviceDataValidator;
//import com.x.data.collection.service.adapter.validation.ValidationResult;
//import com.x.data.collection.service.utils.kafka.KafkaProducerService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ExecutorService;
//
///**
// * MQTT消息处理器
// * 处理从MQTT接收到的设备数据消息
// *
// * @author whj
// */
//@Slf4j
//@Component
//public class MqttMessageProcessor {
//
//    @Autowired
//    private KafkaProducerService kafkaProducerService;
//
//    @Autowired
//    private ExecutorService deviceDataExecutor;
//
//    @Autowired
//    private DeviceDataValidator deviceDataValidator;
//
//    /**
//     * 处理MQTT消息
//     *
//     * @param topic   消息主题
//     * @param payload 消息内容
//     */
//    public void processMessage(String topic, String payload) {
//        // 从主题中提取设备编码
//        String deviceCode = extractDeviceCodeFromTopic(topic);
//
//        // 数据校验
//        ValidationResult validationResult = deviceDataValidator.validateDeviceData(deviceCode, payload);
//        if (!validationResult.isValid()) {
//            log.warn("MQTT设备数据校验失败: topic={}, deviceCode={}, error={}", topic, deviceCode, validationResult.getErrorMessage());
//            return;
//        }
//
//        // 异步处理消息，提高处理速度
//        CompletableFuture.runAsync(() -> {
//            try {
//                if (StringUtils.hasText(deviceCode)) {
//                    // 将数据发送到Kafka
//                    kafkaProducerService.sendMessageAsync(deviceCode, payload);
//                    log.debug("MQTT设备数据已发送到Kafka: deviceCode={}, payload={}", deviceCode, payload);
//                } else {
//                    // 如果无法提取设备编码，则使用topic作为key
//                    kafkaProducerService.sendMessageAsync(topic, payload);
//                    log.debug("MQTT设备数据已发送到Kafka: topic={}, payload={}", topic, payload);
//                }
//            } catch (Exception e) {
//                log.error("处理MQTT设备数据时发生错误: topic={}", topic, e);
//            }
//        }, deviceDataExecutor);
//    }
//
//    /**
//     * 从MQTT主题中提取设备编码
//     *
//     * @param topic MQTT主题
//     * @return 设备编码
//     */
//    private String extractDeviceCodeFromTopic(String topic) {
//        // 假设主题格式为 device/{deviceCode}/data
//        if (topic.startsWith("device/") && topic.endsWith("/data")) {
//            String[] parts = topic.split("/");
//            if (parts.length >= 3) {
//                return parts[1];
//            }
//        }
//        return null;
//    }
//}