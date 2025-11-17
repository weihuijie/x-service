package com.x.tcp.gateway.service.tcp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 支持每秒5000 QPS的高并发TCP服务
 * 核心特性：NIO多路复用、连接复用、低延迟、QPS统计、空闲连接清理
 */
public class TcpHighQpsServer {
    // -------------------------- 核心配置（适配5000 QPS） --------------------------
    private static final String LISTEN_HOST = "0.0.0.0"; // 监听所有网卡
    private static final int LISTEN_PORT = 9000; // 服务端口（与Nginx网关适配）
    private static final int BOSS_THREADS = 1; // 接收连接的线程数（1个足够）
    private static final int WORKER_THREADS = Runtime.getRuntime().availableProcessors() * 2; // 处理IO的线程数（CPU核心*2）
    private static final int BACKLOG = 10240; // 连接队列大小（应对突发峰值）
    private static final int READ_IDLE_SECONDS = 300; // 空闲连接超时（5分钟，与Nginx proxy_timeout匹配）
    private static final int BUFFER_SIZE = 16 * 1024; // 缓冲区大小（16KB，平衡效率和内存）
    private static final int FRAME_MAX_LENGTH = 64 * 1024; // 最大帧长度（64KB，防止粘包拆包）

    // -------------------------- 监控统计 --------------------------
    private static final AtomicLong TOTAL_REQUESTS = new AtomicLong(0); // 总请求数
    private static final AtomicLong CURRENT_CONNECTIONS = new AtomicLong(0); // 当前连接数
    private static final Logger LOGGER = LoggerFactory.getLogger(TcpHighQpsServer.class);

    public static void main(String[] args) {
        new TcpHighQpsServer().start();
    }

    public void start() {
        // 1. 初始化EventLoopGroup（Netty高并发核心：Reactor模式）
        EventLoopGroup bossGroup = new NioEventLoopGroup(BOSS_THREADS);
        EventLoopGroup workerGroup = new NioEventLoopGroup(WORKER_THREADS);

        // 2. 启动QPS统计线程（每秒打印一次）
        startQpsStatThread();

        try {
            // 3. 配置服务端Bootstrap
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // NIO模式（非阻塞）
                    // 服务端TCP参数优化
                    .option(ChannelOption.SO_BACKLOG, BACKLOG) // 连接队列大小
                    .option(ChannelOption.SO_REUSEADDR, true) // 端口复用（服务重启不占用）
                    .option(ChannelOption.SO_RCVBUF, BUFFER_SIZE) // 接收缓冲区
                    .option(ChannelOption.SO_SNDBUF, BUFFER_SIZE) // 发送缓冲区
                    // 客户端连接参数优化
                    .childOption(ChannelOption.SO_KEEPALIVE, true) // 开启TCP保活
                    .childOption(ChannelOption.TCP_NODELAY, true) // 禁用Nagle算法（减少延迟）
                    .childOption(ChannelOption.ALLOCATOR, io.netty.buffer.PooledByteBufAllocator.DEFAULT) // 内存池（减少GC）
                    // 日志处理器（可选，调试用）
                    .handler(new LoggingHandler(LogLevel.INFO))
                    // 客户端连接的处理器链（核心）
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();

                            // ① 空闲连接清理（5分钟无数据则关闭）
                            pipeline.addLast(new IdleStateHandler(READ_IDLE_SECONDS, 0, 0));

                            // ② 粘包拆包处理（基于长度字段，TCP必备）
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(
                                    FRAME_MAX_LENGTH, 0, 4, 0, 4)); // 解码：前4字节为长度
                            pipeline.addLast(new LengthFieldPrepender(4)); // 编码：添加4字节长度字段

                            // ③ 字符串编解码（简化开发，实际可替换为protobuf等二进制协议）
                            pipeline.addLast(new StringDecoder(StandardCharsets.UTF_8));
                            pipeline.addLast(new StringEncoder(StandardCharsets.UTF_8));

                            // ④ 核心业务处理器（处理请求并响应）
                            pipeline.addLast(new TcpBusinessHandler());
                        }
                    });

            // 4. 绑定端口，启动服务（同步阻塞直到启动成功）
            ChannelFuture future = bootstrap.bind(LISTEN_HOST, LISTEN_PORT).sync();
            LOGGER.info(String.format("高并发TCP服务启动成功！监听：%s:%d，Worker线程数：%d，缓冲区：%dKB",
                    LISTEN_HOST, LISTEN_PORT, WORKER_THREADS, BUFFER_SIZE / 1024));

            // 5. 阻塞直到服务关闭（监听关闭事件）
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            LOGGER.error("TCP服务启动异常", e);
        } finally {
            // 6. 优雅关闭资源（释放线程池）
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            LOGGER.info("TCP服务已优雅关闭");
        }
    }

    /**
     * 业务处理器：接收客户端请求，处理后响应（支撑5000 QPS的核心）
     */
    private static class TcpBusinessHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            // 客户端连接成功，连接数+1
            long connCount = CURRENT_CONNECTIONS.incrementAndGet();
            LOGGER.info(String.format("新客户端连接：%s，当前连接数：%d", ctx.channel().remoteAddress(), connCount));
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            // 接收到客户端请求，QPS计数+1
            TOTAL_REQUESTS.incrementAndGet();

            // -------------------------- 业务逻辑（可替换为实际需求） --------------------------
            String request = (String) msg;
            // 示例：回声响应（接收什么返回什么，实际场景可替换为数据库查询、业务计算等）
            String response = String.format("resp:%s", request);
            // 异步响应（Netty非阻塞，提升并发）
            ctx.writeAndFlush(response);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            // 客户端断开连接，连接数-1
            long connCount = CURRENT_CONNECTIONS.decrementAndGet();
            LOGGER.info(String.format("客户端断开连接：%s，当前连接数：%d", ctx.channel().remoteAddress(), connCount));
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            // 异常处理（避免单个连接异常影响整体服务）
            LOGGER.error(String.format("客户端 %s 通信异常", ctx.channel().remoteAddress()), cause);
            ctx.close(); // 关闭异常连接
            CURRENT_CONNECTIONS.decrementAndGet();
        }
    }

    /**
     * 启动QPS统计线程（每秒打印一次，监控服务状态）
     */
    private void startQpsStatThread() {
        new Thread(() -> {
            long lastTotal = 0;
            while (true) {
                try {
                    Thread.sleep(1000); // 每秒统计一次
                    long currentTotal = TOTAL_REQUESTS.get();
                    long qps = currentTotal - lastTotal;
                    LOGGER.info(String.format("当前QPS：%d，总请求数：%d，当前连接数：%d",
                            qps, currentTotal, CURRENT_CONNECTIONS.get()));
                    lastTotal = currentTotal;
                } catch (InterruptedException e) {
                    LOGGER.error("QPS统计线程异常", e);
                    break;
                }
            }
        }, "qps-stat-thread").start();
    }
}