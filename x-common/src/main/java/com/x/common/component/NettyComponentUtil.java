package com.x.common.component;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.CharsetUtil;
import org.springframework.stereotype.Component;

import java.net.SocketAddress;

/**
 * Netty组件工具类
 * 提供Netty网络通信常用操作的封装
 */
@Component
public class NettyComponentUtil {
    
    /**
     * 发送字符串消息到通道
     * @param channel 通道
     * @param message 消息
     * @return 通道future
     */
    public ChannelFuture sendMessage(Channel channel, String message) {
        if (channel == null || !channel.isActive()) {
            throw new IllegalArgumentException("通道不可用");
        }
        
        ByteBuf buffer = Unpooled.copiedBuffer(message, CharsetUtil.UTF_8);
        return channel.writeAndFlush(buffer);
    }
    
    /**
     * 发送字节消息到通道
     * @param channel 通道
     * @param bytes 字节数组
     * @return 通道future
     */
    public ChannelFuture sendMessage(Channel channel, byte[] bytes) {
        if (channel == null || !channel.isActive()) {
            throw new IllegalArgumentException("通道不可用");
        }
        
        ByteBuf buffer = Unpooled.copiedBuffer(bytes);
        return channel.writeAndFlush(buffer);
    }
    
    /**
     * 关闭通道
     * @param channel 通道
     * @return 通道future
     */
    public ChannelFuture closeChannel(Channel channel) {
        if (channel == null) {
            return null;
        }
        
        return channel.close();
    }
    
    /**
     * 获取远程地址
     * @param ctx 通道处理上下文
     * @return 远程地址
     */
    public SocketAddress getRemoteAddress(ChannelHandlerContext ctx) {
        if (ctx == null || ctx.channel() == null) {
            return null;
        }
        
        return ctx.channel().remoteAddress();
    }
    
    /**
     * 获取本地地址
     * @param ctx 通道处理上下文
     * @return 本地地址
     */
    public SocketAddress getLocalAddress(ChannelHandlerContext ctx) {
        if (ctx == null || ctx.channel() == null) {
            return null;
        }
        
        return ctx.channel().localAddress();
    }
    
    /**
     * 检查通道是否活跃
     * @param channel 通道
     * @return 是否活跃
     */
    public boolean isChannelActive(Channel channel) {
        return channel != null && channel.isActive();
    }
}