package com.x.device.access.service;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDubbo
@ComponentScan(value = "com.x",lazyInit = true)
@MapperScan(value = {"com.x.repository.*.mapper"})
public class DeviceAccessServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DeviceAccessServiceApplication.class, args);
    }
}