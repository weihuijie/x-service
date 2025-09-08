package com.x.common.component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Prometheus组件工具类
 * 提供Prometheus监控指标操作常用方法的封装
 */
@Component
public class PrometheusComponentUtil {
    
    @Autowired
    private MeterRegistry meterRegistry;
    
    /**
     * 创建并增加计数器
     * @param name 计数器名称
     * @param description 计数器描述
     * @param tags 标签
     * @param amount 增加的数量
     */
    public void incrementCounter(String name, String description, String[] tags, double amount) {
        Counter.builder(name)
                .description(description)
                .tags(tags)
                .register(meterRegistry)
                .increment(amount);
    }
    
    /**
     * 创建并增加计数器（默认增加1）
     * @param name 计数器名称
     * @param description 计数器描述
     * @param tags 标签
     */
    public void incrementCounter(String name, String description, String[] tags) {
        incrementCounter(name, description, tags, 1.0);
    }
    
    /**
     * 记录方法执行时间
     * @param name 计时器名称
     * @param description 计时器描述
     * @param tags 标签
     * @param task 要执行的任务
     * @param <T> 返回值类型
     * @return 任务执行结果
     */
    public <T> T recordTimer(String name, String description, String[] tags, Task<T> task) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            T result = task.execute();
            sample.stop(Timer.builder(name)
                    .description(description)
                    .tags(tags)
                    .register(meterRegistry));
            return result;
        } catch (Exception e) {
            sample.stop(Timer.builder(name)
                    .description(description)
                    .tags(tags)
                    .register(meterRegistry));
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 记录耗时
     * @param name 计时器名称
     * @param description 计时器描述
     * @param tags 标签
     * @param duration 耗时
     * @param unit 时间单位
     */
    public void recordTime(String name, String description, String[] tags, long duration, TimeUnit unit) {
        Timer.builder(name)
                .description(description)
                .tags(tags)
                .register(meterRegistry)
                .record(duration, unit);
    }
    
    /**
     * 任务接口
     * @param <T> 返回值类型
     */
    public interface Task<T> {
        T execute() throws Exception;
    }
}