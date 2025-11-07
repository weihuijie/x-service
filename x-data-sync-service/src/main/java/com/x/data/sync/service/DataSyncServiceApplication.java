package com.x.data.sync.service;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(value = {"com.x"})
@MapperScan(value = {"com.x.repository.*.mapper"})
public class DataSyncServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DataSyncServiceApplication.class, args);
    }
}