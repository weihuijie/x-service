package com.x.alert.notice.service.rabbitmq;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.rabbitmq.client.*;
import com.x.alert.notice.service.service.AlertMsgDubboServiceImpl;
import com.x.repository.service.entity.AlertMsgEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * RabbitMQ消息消费者客户端
 * 用于从RabbitMQ队列中消费传感器告警消息
 */
@Slf4j
@Component
public class RabbitMQConsumer {
    
    @Autowired
    private RabbitMQConfigProperties rabbitMQConfig;

    @Autowired
    private AlertMsgDubboServiceImpl alertMsgDubboService;
    
    private Connection connection;
    private Channel channel;
    
    /**
     * 初始化RabbitMQ连接和消费者
     */
    @PostConstruct
    public void init() {
        try {
            connectToRabbitMQ();
            startConsuming();
        } catch (Exception e) {
            log.error("初始化RabbitMQ消费者失败", e);
        }
    }
    
    /**
     * 建立RabbitMQ连接
     */
    private void connectToRabbitMQ() throws IOException, TimeoutException {
        // 创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitMQConfig.getHost());
        factory.setPort(rabbitMQConfig.getPort());
        factory.setUsername(rabbitMQConfig.getUsername());
        factory.setPassword(rabbitMQConfig.getPassword());
        factory.setVirtualHost(rabbitMQConfig.getVirtualHost());
        factory.setConnectionTimeout(rabbitMQConfig.getConnectionTimeout());
        
        // 建立连接和信道
        connection = factory.newConnection("Alert-Notice-Service-Consumer");
        channel = connection.createChannel();
        
        // 声明队列（确保队列存在）
        channel.queueDeclare(
                rabbitMQConfig.getAlertQueue(),
                true,  // durable
                false, // exclusive
                false, // autoDelete
                null   // arguments
        );
        
        // 设置QoS，每次只处理一条消息
        channel.basicQos(1);
        
        log.info("成功连接到RabbitMQ，队列: {}", rabbitMQConfig.getAlertQueue());
    }
    
    /**
     * 开始消费消息
     */
    private void startConsuming() throws IOException {
        // 创建消费者回调
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody());
            log.info("收到RabbitMQ消息: {}", message);
            
            try {
                // 处理消息
                processMessage(message);
                // 手动确认消息
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                log.debug("消息处理完成并已确认");
            } catch (Exception e) {
                log.error("处理消息失败: {}", message, e);
                // 拒绝消息并重新入队
                channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
            }
        };
        
        // 取消消费回调
        CancelCallback cancelCallback = consumerTag -> {
            log.warn("消费者被取消: {}", consumerTag);
        };
        
        // 开始消费消息
        channel.basicConsume(
                rabbitMQConfig.getAlertQueue(),
                false, // autoAck设为false，手动确认
                deliverCallback,
                cancelCallback
        );
        log.info("开始消费RabbitMQ队列 {} 中的消息", rabbitMQConfig.getAlertQueue());
    }
    
    /**
     * 处理接收到的消息
     * @param message 消息内容
     */
    private void processMessage(String message) {
        try {
            // 解析消息为JSON对象
            JSONObject jsonData = JSON.parseObject(message);
            
            // 构建告警消息
            AlertMsgEntity alertMsgEntity = AlertMsgEntity.builder()
                    .deviceId(jsonData.getLong("deviceId"))
                    .pointId(jsonData.getLong("id"))
                    .pointAddr(jsonData.getString("pointAddr"))
                    .pointValue(jsonData.getString("pointValue"))
                    .build();
            boolean result =  alertMsgDubboService.submit(alertMsgEntity).isSuccess();
            if (!result){
                log.error("保存告警消息失败: {}", message);
                throw new RuntimeException("保存告警消息失败");
            }
        } catch (Exception e) {
            log.error("解析或处理消息失败: {}", message, e);
            throw new RuntimeException("消息处理失败", e);
        }
    }
    
    /**
     * 关闭RabbitMQ连接
     */
    @PreDestroy
    public void destroy() {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
                log.info("RabbitMQ信道已关闭");
            }
            if (connection != null && connection.isOpen()) {
                connection.close();
                log.info("RabbitMQ连接已关闭");
            }
        } catch (IOException | TimeoutException e) {
            log.error("关闭RabbitMQ连接时出错", e);
        }
    }
}