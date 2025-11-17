package com.x.data.collection.tcp.service.tcp;

import com.alibaba.fastjson2.JSONObject;
import com.x.repository.service.entity.DeviceInfoEntity;
import com.x.data.collection.tcp.service.utils.kafka.KafkaProducerService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Spring Boot 3 集成 Netty TCP 服务（实现 SmartLifecycle 接口，自动启停）
 */
@Slf4j
@Component
public class TcpServer implements SmartLifecycle {
    // Netty 核心组件（原子引用确保线程安全）
    private final AtomicReference<EventLoopGroup> bossGroup = new AtomicReference<>();
    private final AtomicReference<EventLoopGroup> workerGroup = new AtomicReference<>();
    private final AtomicReference<Channel> serverChannel = new AtomicReference<>();

    // 服务状态（volatile 保证多线程可见性）
    private volatile boolean isRunning = false;
    private volatile boolean isAutoStartup = true;
    private static final int PHASE = 99999; // 启动顺序（数值越大，启动越晚）

    @Resource
    private TcpServerProperties tcpConfig; // 注入配置类

    // 注入业务线程池（Spring 管理）
    @Resource(name = "tcpBusinessExecutor")
    private Executor tcpBusinessExecutor;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    /**
     * Spring 上下文刷新完成后，自动调用启动 TCP 服务
     */
    @Override
    public void start() {
        if (isRunning) {
            log.warn("TCP 服务已在运行，无需重复启动");
            return;
        }

        try {
            // 1. 初始化 Netty 线程组
            EventLoopGroup boss = new NioEventLoopGroup(tcpConfig.getBossThreads());
            EventLoopGroup worker = new NioEventLoopGroup(tcpConfig.getWorkerThreads());
            bossGroup.set(boss);
            workerGroup.set(worker);

            // 2. 配置 Netty 服务端
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    // 服务端 TCP 参数
                    .option(ChannelOption.SO_BACKLOG, tcpConfig.getBacklog())
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.SO_RCVBUF, tcpConfig.getBufferSize())
                    .option(ChannelOption.SO_SNDBUF, tcpConfig.getBufferSize())
                    // 客户端连接参数
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.ALLOCATOR, io.netty.buffer.PooledByteBufAllocator.DEFAULT)
                    // 处理器链
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            // 空闲连接清理
                            pipeline.addLast(new IdleStateHandler(tcpConfig.getReadIdleSeconds(), 0, 0));
                            // 粘包拆包处理 (二进制)
//                            pipeline.addLast(new LengthFieldBasedFrameDecoder(tcpConfig.getFrameMaxLength(), 0, 4, 0, 4));
//                            pipeline.addLast(new LengthFieldPrepender(4));
                            // 基于换行符的文本解码器
                            // 最大帧长度：10240（10KB），超过则丢弃 , 换行符解码
//                            pipeline.addLast(new LineBasedFrameDecoder(10240));
                            // 最大 JSON 长度 10KB，自动识别 JSON 边界
                            pipeline.addLast(new JsonObjectDecoder(10240));
                            // 字符串编解码
                            pipeline.addLast(new StringDecoder(StandardCharsets.UTF_8));
                            pipeline.addLast(new StringEncoder(StandardCharsets.UTF_8));
                            // 核心业务处理器
                            pipeline.addLast(new TcpBusinessHandler());
                        }
                    });

            // 3. 绑定端口，启动监听（同步阻塞直到绑定成功）
            ChannelFuture bindFuture = bootstrap.bind(tcpConfig.getHost(), tcpConfig.getPort()).sync();
            Channel channel = bindFuture.channel();
            serverChannel.set(channel);
            isRunning = true;

            // 4. 监听通道关闭事件（服务关闭时触发）
            channel.closeFuture().addListener(future -> {
                log.info("TCP 监听通道已关闭");
                stop();
            });

            log.info("TCP 服务启动成功！监听地址：{}:{}，Worker线程数：{}",
                    tcpConfig.getHost(), tcpConfig.getPort(), tcpConfig.getWorkerThreads());

        } catch (InterruptedException e) {
            log.error("TCP 服务启动失败", e);
            Thread.currentThread().interrupt();
            stop(); // 启动失败时释放资源
        }
    }

    /**
     * Spring 上下文关闭时，自动调用关闭 TCP 服务
     */
    @Override
    public void stop() {
        if (!isRunning) {
            log.warn("TCP 服务已关闭，无需重复操作");
            return;
        }

        log.info("开始关闭 TCP 服务，正在释放资源...");
        isRunning = false;

        try {
            // 1. 关闭监听通道（停止接收新连接）
            Channel channel = serverChannel.get();
            if (channel != null && channel.isActive()) {
                channel.close().sync();
            }

            // 2. 优雅关闭线程组（等待现有任务处理完成）
            EventLoopGroup worker = workerGroup.get();
            if (worker != null) {
                worker.shutdownGracefully().sync();
            }
            EventLoopGroup boss = bossGroup.get();
            if (boss != null) {
                boss.shutdownGracefully().sync();
            }

            // 3. 清空原子引用
            serverChannel.set(null);
            workerGroup.set(null);
            bossGroup.set(null);

            log.info("TCP 服务已优雅关闭，所有资源释放完成");

        } catch (InterruptedException e) {
            log.error("TCP 服务关闭异常", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 核心业务处理器（回声服务，可替换为实际业务）
     */
    private class TcpBusinessHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) {
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            String request = (String) msg;
            log.info("IO线程 [{}] 接收请求：{}", Thread.currentThread().getName(), request);

            // 保存 Channel 状态（避免异步处理时连接已关闭）
            boolean isChannelActive = ctx.channel().isActive();
            if (!isChannelActive) {
                log.warn("客户端 [{}] 连接已关闭，丢弃请求：{}", ctx.channel().remoteAddress(), request);
                return;
            }

            // 将业务逻辑提交到独立线程池
            CompletableFuture.supplyAsync(() -> {
                        // -------------- 耗时业务逻辑（运行在业务线程池）--------------
                        log.info("业务线程 [{}] 处理请求：{}", Thread.currentThread().getName(), request);
                        try {
                            // 模拟耗时操作：如 JSON 解析、数据库存储、第三方接口调用（50-200ms）
                            // 实际业务逻辑示例：
                            // 1. 解析 JSON 请求（如转成 DeviceData 对象）
                             DeviceInfoEntity data = JSONObject.parseObject(request, DeviceInfoEntity.class);
                            // 2. 数据库操作（如保存设备数据）
                            // deviceDataMapper.insert(data);
                            // 3. 复杂计算或第三方接口调用
                            // String result = thirdPartyService.process(data);
                            kafkaProducerService.sendMessageAsync(data.getDeviceCode(),JSONObject.toJSONString(data));

                            return "{\"code\":200,\"msg\":\"success\",\"data\":\"处理完成\"}"; // 业务处理结果
                        } catch (Exception e) {
                            log.error("业务线程处理请求失败：{}", request, e);
                            return "{\"code\":500,\"msg\":\"business error\"}"; // 异常结果
                        }
                        // ----------------------------------------------------------
                    }, tcpBusinessExecutor)
                    // 3. 异步处理完成后，回调响应客户端（运行在 IO 线程或业务线程，Netty 自动保证线程安全）
                    .whenComplete((response, throwable) -> {
                        if (throwable != null) {
                            // 异步处理时抛出异常（如线程池拒绝策略触发）
                            log.error("异步处理请求失败：{}", request, throwable);
                        } else {
                            // 异步处理成功，返回响应
                            log.info("业务线程处理完成");
                        }
                    });

            // 3. IO 线程立即返回，继续处理其他客户端请求（不阻塞）
            log.info("IO线程 [{}] 已提交请求到业务线程池，继续处理下一个请求", Thread.currentThread().getName());
            ctx.writeAndFlush("{\"code\":200,\"msg\":\"success\"}");
        }
        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            // 异常处理（避免单个连接影响整体服务）
            log.error("客户端 {} 通信异常", ctx.channel().remoteAddress(), cause);
            ctx.close();
        }
    }

    // -------------------------- SmartLifecycle 接口实现 --------------------------
    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public boolean isAutoStartup() {
        return isAutoStartup; // 开启自动启动
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run(); // 通知 Spring 关闭完成
    }

    @Override
    public int getPhase() {
        return PHASE; // 启动顺序（确保在 Spring 其他组件初始化后启动）
    }
}