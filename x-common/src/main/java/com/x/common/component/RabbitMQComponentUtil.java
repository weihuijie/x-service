package com.x.common.component;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ组件工具类
 * 提供RabbitMQ消息发送等常用操作的封装
 */
@Component
public class RabbitMQComponentUtil {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    /**
     * 发送消息到指定交换机和路由键
     * @param exchange 交换机名称
     * @param routingKey 路由键
     * @param message 消息内容
     */
    public void sendMessage(String exchange, String routingKey, Object message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }
    
    /**
     * 发送消息到指定队列
     * @param queue 队列名称
     * @param message 消息内容
     */
    public void sendMessageToQueue(String queue, Object message) {
        rabbitTemplate.convertAndSend(queue, message);
    }
    
    /**
     * 发送消息并设置消息ID
     * @param exchange 交换机名称
     * @param routingKey 路由键
     * @param message 消息内容
     * @param correlationId 消息ID
     */
    public void sendMessageWithId(String exchange, String routingKey, Object message, String correlationId) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message, m -> {
            m.getMessageProperties().setCorrelationId(correlationId);
            return m;
        });
    }
}