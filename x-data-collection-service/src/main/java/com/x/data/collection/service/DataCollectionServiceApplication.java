package com.x.data.collection.service;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 数据采集服务启动类
 * 
 * @author whj
 */
@SpringBootApplication
@EnableDubbo
public class DataCollectionServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DataCollectionServiceApplication.class, args);
    }
}