package com.x.data.collection.mqtt.service.mqtt;

import com.alibaba.fastjson2.JSONObject;
import com.x.data.collection.mqtt.service.utils.kafka.KafkaProducerService;
import com.x.repository.service.entity.DeviceInfoEntity;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Component
@Slf4j
public class MqttClientService {

    private final MqttProperties mqttProperties;

    private final KafkaProducerService kafkaProducerService;

    // 注入业务线程池（Spring 管理）
    private final Executor mqttBusinessExecutor;

    private MqttClient mqttClient;

    public MqttClientService(MqttProperties mqttProperties, KafkaProducerService kafkaProducerService, Executor mqttBusinessExecutor) {
        this.mqttProperties = mqttProperties;
        this.kafkaProducerService = kafkaProducerService;
        this.mqttBusinessExecutor = mqttBusinessExecutor;
    }

    @PostConstruct
    public void connect() {
        try {
            log.info("正在连接MQTT服务器...");
            mqttClient = new MqttClient(mqttProperties.getUrl(), mqttProperties.getClientId(), new MemoryPersistence());

            MqttConnectOptions options = getMqttConnectOptions();

            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    log.error("MQTT连接断开", cause);
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                    connect();
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    String payload = new String(message.getPayload());
                    log.debug("IO线程 [{}] 接收MQTT消息：topic={}, message={}", Thread.currentThread().getName(), topic, payload);

                    // 将业务逻辑提交到独立线程池
                    CompletableFuture.supplyAsync(() -> {
                                // -------------- 耗时业务逻辑（运行在业务线程池）--------------
                                log.debug("业务线程 [{}] 处理MQTT消息：{}", Thread.currentThread().getName(), payload);
                                try {
                                    // 实际业务逻辑示例：
                                    // 解析 JSON 请求
                                    DeviceInfoEntity data = JSONObject.parseObject(payload, DeviceInfoEntity.class);
                                    kafkaProducerService.sendMessageAsync(data.getDeviceCode(), JSONObject.toJSONString(data));

                                    return "{\"code\":200,\"msg\":\"success\",\"data\":\"处理完成\"}"; // 业务处理结果
                                } catch (Exception e) {
                                    log.error("业务线程处理MQTT消息失败：{}", payload, e);
                                    return "{\"code\":500,\"msg\":\"business error\"}"; // 异常结果
                                }
                                // ----------------------------------------------------------
                            }, mqttBusinessExecutor)
                            .whenComplete((response, throwable) -> {
                                if (throwable != null) {
                                    // 异步处理时抛出异常（如线程池拒绝策略触发）
                                    log.error("异步处理MQTT消息失败：{}", payload, throwable);
                                } else {
                                    // 异步处理成功，返回响应
                                    log.debug("业务线程处理完成");
                                }
                            });

                    log.debug("IO线程 [{}] 已提交MQTT消息到业务线程池，继续处理下一个消息", Thread.currentThread().getName());
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // 消息发送完成回调
                }
            });

            mqttClient.connect(options);
            mqttClient.subscribe(mqttProperties.getDefaultTopic(), mqttProperties.getQos());
            log.info("MQTT客户端连接成功！服务器地址：{}，订阅主题：{}", mqttProperties.getUrl(), mqttProperties.getDefaultTopic());
        } catch (MqttException e) {
            log.error("MQTT客户端连接失败", e);
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            connect();
        }
    }

    private MqttConnectOptions getMqttConnectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setConnectionTimeout(mqttProperties.getTimeout());
        options.setKeepAliveInterval(30);

        if (mqttProperties.getUsername() != null && !mqttProperties.getUsername().isEmpty()) {
            options.setUserName(mqttProperties.getUsername());
        }

        if (mqttProperties.getPassword() != null && !mqttProperties.getPassword().isEmpty()) {
            options.setPassword(mqttProperties.getPassword().toCharArray());
        }
        return options;
    }

    @PreDestroy
    public void disconnect() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                mqttClient.close();
                log.info("MQTT客户端已断开连接");
            }
        } catch (MqttException e) {
            log.error("MQTT客户端断开连接时发生错误", e);
        }
    }

    public void publish(String topic, String message) throws MqttException {
        MqttMessage mqttMessage = new MqttMessage(message.getBytes());
        mqttMessage.setQos(mqttProperties.getQos());
        mqttClient.publish(topic, mqttMessage);
    }
}