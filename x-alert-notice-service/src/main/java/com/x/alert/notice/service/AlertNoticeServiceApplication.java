package com.x.alert.notice.service;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 告警通知服务启动类
 * 
 * @author whj
 */
@SpringBootApplication
@EnableDubbo
@ComponentScan(value = "com.x")
@MapperScan(value = {"com.x.repository.*.mapper"})
public class AlertNoticeServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AlertNoticeServiceApplication.class, args);
    }
}