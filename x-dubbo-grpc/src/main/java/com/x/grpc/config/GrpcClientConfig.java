package com.x.grpc.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GrpcClientConfig {

    public ManagedChannel deviceServiceChannel() {
        // 创建到设备管理服务的gRPC通道
        return ManagedChannelBuilder.forAddress("localhost", 9091)
                .usePlaintext()
                .build();
    }

    public ManagedChannel authServiceChannel() {
        // 创建到认证服务的gRPC通道
        return ManagedChannelBuilder.forAddress("localhost", 9092)
                .usePlaintext()
                .build();
    }

    public ManagedChannel dataServiceChannel() {
        // 创建到数据处理服务的gRPC通道
        return ManagedChannelBuilder.forAddress("localhost", 9093)
                .usePlaintext()
                .build();
    }
}
