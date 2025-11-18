package com.x.data.collection.mqtt.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * MQTT数据采集服务启动类
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.x.data.*"})
public class DataCollectionMqttServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DataCollectionMqttServiceApplication.class, args);

    }
}