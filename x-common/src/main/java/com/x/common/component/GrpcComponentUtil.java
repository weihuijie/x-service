package com.x.common.component;

import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * gRPC组件工具类
 * 提供gRPC客户端常用操作的封装
 */
@Component
public class GrpcComponentUtil {

    /**
     * 安全关闭gRPC通道
     * @param channel gRPC通道
     * @param timeout 超时时间
     * @param unit 时间单位
     * @throws InterruptedException 中断异常
     */
    public void shutdownChannel(ManagedChannel channel, long timeout, TimeUnit unit) throws InterruptedException {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown().awaitTermination(timeout, unit);
        }
    }

    /**
     * 检查gRPC通道是否可用
     * @param channel gRPC通道
     * @return 是否可用
     */
    public boolean isChannelAvailable(ManagedChannel channel) {
        if (channel == null) {
            return false;
        }

        try {
            // 尝试连接
            channel.getState(true);
            return !channel.isShutdown() && !channel.isTerminated();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 处理gRPC调用异常
     * @param e 异常
     * @return 错误信息
     */
    public String handleGrpcException(Exception e) {
        if (e instanceof StatusRuntimeException) {
            StatusRuntimeException statusException = (StatusRuntimeException) e;
            return "gRPC调用失败: " + statusException.getStatus().getCode() +
                   ", 描述: " + statusException.getStatus().getDescription();
        } else {
            return "gRPC调用异常: " + e.getMessage();
        }
    }
}
