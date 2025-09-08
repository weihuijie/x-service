package com.x.demo.service.service;

import com.x.demo.service.entity.DeviceData;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeviceAccessService {
    
    @Autowired
    private DataProcessingService dataProcessingService;
    
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelFuture channelFuture;
    
    private int port = 8001; // Netty服务器端口
    
    @PostConstruct
    public void startNettyServer() {
        new Thread(() -> {
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();
            
            try {
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                ChannelPipeline pipeline = ch.pipeline();
                                pipeline.addLast(new StringDecoder());
                                pipeline.addLast(new StringEncoder());
                                pipeline.addLast(new DeviceDataHandler());
                            }
                        })
                        .option(ChannelOption.SO_BACKLOG, 128)
                        .childOption(ChannelOption.SO_KEEPALIVE, true);
                
                channelFuture = bootstrap.bind(port).sync();
                System.out.println("Netty服务器已启动，监听端口: " + port);
                
                channelFuture.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                shutdown();
            }
        }).start();
    }
    
    @PreDestroy
    public void shutdown() {
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        System.out.println("Netty服务器已关闭");
    }
    
    // 设备数据处理器
    private class DeviceDataHandler extends SimpleChannelInboundHandler<String> {
        
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("设备已连接: " + ctx.channel().remoteAddress());
        }
        
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            System.out.println("收到设备数据: " + msg);
            
            // 解析设备数据（简化处理）
            // 格式: deviceId:dataType:data
            String[] parts = msg.split(":");
            if (parts.length >= 3) {
                DeviceData deviceData = new DeviceData();
                deviceData.setDeviceId(parts[0]);
                deviceData.setDataType(parts[1]);
                deviceData.setData(parts[2]);
                deviceData.setTimestamp(System.currentTimeMillis());
                
                // 处理设备数据
                dataProcessingService.processDeviceDataAsync(deviceData);
                
                // 发送确认消息
                ctx.writeAndFlush("数据接收成功\n");
            } else {
                ctx.writeAndFlush("数据格式错误\n");
            }
        }
        
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }
    }
}