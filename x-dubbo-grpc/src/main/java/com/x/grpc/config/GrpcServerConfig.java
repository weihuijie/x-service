package com.x.grpc.config;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class GrpcServerConfig {

    private Server server;

    public Server grpcServer() throws IOException {
        // 创建gRPC服务器，监听9090端口
        server = ServerBuilder.forPort(9090)
                .build();

        return server;
    }
}
