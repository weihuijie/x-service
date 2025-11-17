package com.x.tcp.gateway.service.tcp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;

/**
 * TCP服务压测客户端（模拟每秒5000 QPS）
 */
public class TcpPressureTestClient {
    private static final String SERVER_HOST = "127.0.0.1"; // 服务端IP
    private static final int SERVER_PORT = 9000; // 服务端端口
    private static final int CLIENT_THREADS = 10; // 压测客户端线程数
    private static final int CONNECTIONS_PER_THREAD = 50; // 每个线程的连接数（总连接数=10*50=500）
    private static final int REQUESTS_PER_SECOND = 5000; // 目标QPS
    private static final AtomicLong TOTAL_SENT = new AtomicLong(0);
    private static final AtomicLong TOTAL_RECEIVED = new AtomicLong(0);
    private static final Logger LOGGER = LoggerFactory.getLogger(TcpPressureTestClient.class);

    public static void main(String[] args) {
        // 启动QPS统计线程
        startStatThread();

        // 初始化客户端EventLoopGroup
        EventLoopGroup group = new NioEventLoopGroup(CLIENT_THREADS);

        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.ALLOCATOR, io.netty.buffer.PooledByteBufAllocator.DEFAULT)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(64 * 1024, 0, 4, 0, 4));
                            pipeline.addLast(new LengthFieldPrepender(4));
                            pipeline.addLast(new StringDecoder(StandardCharsets.UTF_8));
                            pipeline.addLast(new StringEncoder(StandardCharsets.UTF_8));
                            pipeline.addLast(new ClientHandler());
                        }
                    });

            // 启动多个线程，每个线程创建多个连接并发送请求
            for (int i = 0; i < CLIENT_THREADS; i++) {
                new Thread(() -> {
                    for (int j = 0; j < CONNECTIONS_PER_THREAD; j++) {
                        try {
                            // 建立连接
                            ChannelFuture future = bootstrap.connect(SERVER_HOST, SERVER_PORT).sync();
                            Channel channel = future.channel();
                            LOGGER.info("创建压测连接：{}", channel.remoteAddress());

                            // 每个连接循环发送请求（控制QPS）
                            while (true) {
                                String request = "test_qps_" + System.currentTimeMillis();
                                channel.writeAndFlush(request);
                                TOTAL_SENT.incrementAndGet();

                                // 控制QPS：总目标5000，每个连接发送频率=5000/(10*50)=10次/秒
                                Thread.sleep(100);
                            }
                        } catch (Exception e) {
                            LOGGER.error("压测连接异常", e);
                        }
                    }
                }, "pressure-thread-" + i).start();
            }

            // 阻塞主线程
            synchronized (TcpPressureTestClient.class) {
                TcpPressureTestClient.class.wait();
            }
        } catch (Exception e) {
            LOGGER.error("压测客户端异常", e);
        } finally {
            group.shutdownGracefully();
        }
    }

    private static class ClientHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            // 接收服务端响应，计数+1
            TOTAL_RECEIVED.incrementAndGet();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            LOGGER.error("压测连接异常", cause);
            ctx.close();
        }
    }

    private static void startStatThread() {
        new Thread(() -> {
            long lastSent = 0;
            long lastReceived = 0;
            while (true) {
                try {
                    Thread.sleep(1000);
                    long currentSent = TOTAL_SENT.get();
                    long currentReceived = TOTAL_RECEIVED.get();
                    long sendQps = currentSent - lastSent;
                    long recvQps = currentReceived - lastReceived;
                    LOGGER.info(String.format("压测发送QPS：%d，接收响应QPS：%d，成功率：%.2f%%",
                            sendQps, recvQps, recvQps * 100.0 / sendQps));
                    lastSent = currentSent;
                    lastReceived = currentReceived;
                } catch (InterruptedException e) {
                    LOGGER.error("压测统计线程异常", e);
                    break;
                }
            }
        }, "pressure-stat-thread").start();
    }
}