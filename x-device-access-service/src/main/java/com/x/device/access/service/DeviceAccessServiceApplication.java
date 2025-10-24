package com.x.device.access.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import net.devh.boot.grpc.server.autoconfigure.GrpcServerAutoConfiguration;

@SpringBootApplication(exclude = {GrpcServerAutoConfiguration.class})
public class DeviceAccessServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DeviceAccessServiceApplication.class, args);
    }
}