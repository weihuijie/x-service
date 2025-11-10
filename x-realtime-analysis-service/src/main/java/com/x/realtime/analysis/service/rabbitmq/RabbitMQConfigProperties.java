package com.x.realtime.analysis.service.rabbitmq;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.Serial;
import java.io.Serializable;

/**
 * RabbitMQ 连接配置
 *
 * @author whj
 */
@Data
@Component
@ConfigurationProperties(prefix = "rabbitmq")
public class RabbitMQConfigProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    private String host = "localhost"; // 默认值
    private Integer port = 5672;
    private String username = "guest";
    private String password = "guest";
    private String virtualHost = "/";
    private String alertQueue = "sensor.alert.queue"; // 告警队列名
    private Integer connectionTimeout = 5000; // 连接超时时间（毫秒）
    private Integer retryCount = 3; // 消息发送重试次数
    private Long retryInterval = 1000L; // 重试间隔（毫秒）
    // 扩展配置类
    private String exchangeName = "sensor.alert.exchange";
    private String routingKey = "sensor.alert.key";
}