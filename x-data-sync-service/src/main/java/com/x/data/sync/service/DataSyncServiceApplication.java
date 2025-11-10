package com.x.data.sync.service;

import com.x.data.sync.service.IotDB.DeviceDataService;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(value = {"com.x"})
@MapperScan(value = {"com.x.repository.*.mapper"})
@RequiredArgsConstructor
public class DataSyncServiceApplication implements CommandLineRunner {

    private final DeviceDataService deviceDataService;
    public static void main(String[] args) {
        SpringApplication.run(DataSyncServiceApplication.class, args);
    }

    // 应用启动时初始化 IotDB 存储组
    @Override
    public void run(String... args) {
        deviceDataService.initStorageGroup();
        System.out.println("IotDB 存储组初始化完成！");
    }
}