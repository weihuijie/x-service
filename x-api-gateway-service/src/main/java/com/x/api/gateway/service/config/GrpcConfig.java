package com.x.api.gateway.service.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

/**
 * gRPC配置类
 * 用于解决gRPC相关依赖冲突问题
 */
@Configuration
@ConditionalOnClass({
    io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder.class,
    io.grpc.netty.NettyChannelBuilder.class
})
public class GrpcConfig {
    
    // 在这里可以添加gRPC相关的配置
    // 目前保持空实现，主要是为了解决类加载问题
    
}