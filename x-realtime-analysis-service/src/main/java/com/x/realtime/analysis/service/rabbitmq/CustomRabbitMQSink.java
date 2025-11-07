package com.x.realtime.analysis.service.rabbitmq;

import com.alibaba.fastjson2.JSONObject;
import com.rabbitmq.client.*;
import com.x.realtime.analysis.service.flink.SensorData;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 基于 Rabbit MQ 官方客户端的自定义 Flink Sink
 * 支持：连接复用、失败重试、资源自动释放、Checkpoint 兼容
 */
@Slf4j
public class CustomRabbitMQSink extends RichSinkFunction<SensorData> {
    // RabbitMQ 核心对象（每个并行实例一个连接+信道）
    private transient Connection rabbitConnection;
    private transient Channel rabbitChannel;

    // 配置参数（从外部注入）
    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String virtualHost;
    private final String alertQueue;
    private final int connectionTimeout;
    private final int retryCount;
    private final long retryInterval;
    private final String exchangeName;
    private final String routingKey;

    // 构造器：注入配置
    public CustomRabbitMQSink(RabbitMQConfigProperties config) {
        this.host = config.getHost();
        this.port = config.getPort();
        this.username = config.getUsername();
        this.password = config.getPassword();
        this.virtualHost = config.getVirtualHost();
        this.alertQueue = config.getAlertQueue();
        this.connectionTimeout = config.getConnectionTimeout();
        this.retryCount = config.getRetryCount();
        this.retryInterval = config.getRetryInterval();
        this.exchangeName = config.getExchangeName();
        this.routingKey = config.getRoutingKey();
    }

    /**
     * 初始化：创建 RabbitMQ 连接和信道（Flink 算子启动时执行一次）
     */
    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        // 注册指标
        createRabbitConnectionAndChannel();
    }

    /**
     * 处理每条数据：序列化并发送到 RabbitMQ（每条数据执行一次）
     */
    @Override
    public void invoke(SensorData sensorData, Context context) throws Exception {
        if (sensorData == null) {
            log.warn("跳过空数据，不发送到 RabbitMQ");
            return;
        }

        // 1. 序列化 SensorData 为 JSON 字节数组
        byte[] messageBody;
        try {
            messageBody = JSONObject.toJSONString(sensorData).getBytes();
        } catch (Exception e) {
            log.error("SensorData 序列化失败，数据：{}", sensorData, e);
            return;
        }

        // 2. 发送消息（支持重试）
        sendWithRetry(alertQueue, messageBody);
    }

    /**
     * 资源释放：关闭信道和连接（Flink 算子停止时执行一次）
     */
    @Override
    public void close() throws Exception {
        super.close();
        // 先关信道，再关连接
        if (rabbitChannel != null && rabbitChannel.isOpen()) {
            try {
                rabbitChannel.close();
                log.info("RabbitMQ 信道已关闭");
            } catch (IOException | TimeoutException e) {
                log.error("关闭 RabbitMQ 信道失败", e);
            }
        }
        if (rabbitConnection != null && rabbitConnection.isOpen()) {
            try {
                rabbitConnection.close();
                log.info("RabbitMQ 连接已关闭");
            } catch (IOException e) {
                log.error("关闭 RabbitMQ 连接失败", e);
            }
        }
    }

    /**
     * 创建 RabbitMQ 连接和信道（封装连接逻辑，支持重连）
     */
    private void createRabbitConnectionAndChannel() throws IOException, TimeoutException {
        // 1. 构建 RabbitMQ 连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setVirtualHost(virtualHost);
        factory.setConnectionTimeout(connectionTimeout);

        // 2. 创建连接和信道
        rabbitConnection = factory.newConnection("Flink-Custom-RabbitMQ-Sink");
        rabbitChannel = rabbitConnection.createChannel();

        // 3. 声明队列（确保队列存在，不存在则自动创建）
        // durable=true：队列持久化（重启 RabbitMQ 后队列不丢失）
        // exclusive=false：允许其他连接访问队列
        // autoDelete=false：不自动删除队列（无消费者时不删除）
        rabbitChannel.queueDeclare(
                alertQueue,
                true,
                false,
                false,
                null // 额外参数（如队列过期时间）
        );
        rabbitChannel.exchangeDeclare(
                exchangeName,
                BuiltinExchangeType.TOPIC, // 交换机类型（Topic/Direct/Fanout）
                true // 交换机持久化
        );
        // 绑定队列和交换机
        rabbitChannel.queueBind(
                alertQueue,
                exchangeName,
                routingKey
        );
        log.info("RabbitMQ 连接初始化成功：host={}, port={}, queue={}",
                host, port, alertQueue);
    }

    /**
     * 发送消息（带重试机制）
     * @param queueName 队列名
     * @param messageBody 消息体（字节数组）
     */
    private void sendWithRetry(String queueName, byte[] messageBody) throws Exception {
        int retryCount = 0;
        while (retryCount < this.retryCount) {
            try {
                // 检查信道是否正常，异常则重建连接
                if (rabbitChannel == null || !rabbitChannel.isOpen() || !rabbitConnection.isOpen()) {
                    log.warn("RabbitMQ 连接/信道已断开，尝试重建");
                    createRabbitConnectionAndChannel();
                }

                // 发送消息
                // routingKey=队列名（简单模式）
                // mandatory=true：如果消息无法路由到队列，会触发 ReturnListener（此处简化处理）
                // BasicProperties：设置消息持久化（deliveryMode=2）
                rabbitChannel.basicPublish(
                        exchangeName, // 交换机名
                        routingKey,  // 路由键
                        MessageProperties.PERSISTENT_TEXT_PLAIN,
                        messageBody
                );

                log.debug("消息发送到 RabbitMQ 成功：queue={}, data={}",
                        queueName, new String(messageBody));
                return; // 发送成功，退出重试循环

            } catch (Exception e) {
                retryCount++;
                if (retryCount >= this.retryCount) {
                    // 重试次数耗尽，抛出异常（Flink 会根据 Checkpoint 重试整个算子）
                    log.error("消息发送到 RabbitMQ 失败，重试 {} 次后仍失败", this.retryCount, e);
                    throw new RuntimeException("RabbitMQ 消息发送失败", e);
                }
                // 重试间隔（避免频繁重试）
                Thread.sleep(this.retryInterval * retryCount); // 指数退避（1s, 2s, 3s...）
                log.warn("消息发送失败，将进行第 {} 次重试：{}", retryCount + 1, e.getMessage());
            }
        }
    }
}