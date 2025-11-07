package com.x.alert.notice.service;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDubbo
@ComponentScan(value = "com.x")
@MapperScan(value = {"com.x.repository.*.mapper"})
public class AlertNoticeServiceApplication {
    /**
     * 启动告警通知服务应用
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(AlertNoticeServiceApplication.class, args);
    }
}