package com.x.data.collection.mqtt.service.mqtt;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.Serial;
import java.io.Serializable;

@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "mqtt.broker")
public class MqttProperties  implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String url = "tcp://localhost:1883";
    private String username;
    private String password;
    private String clientId = "mqtt-data-collector-client";
    private String defaultTopic = "device/data";
    private int qos = 1;
    private int timeout = 30;
}