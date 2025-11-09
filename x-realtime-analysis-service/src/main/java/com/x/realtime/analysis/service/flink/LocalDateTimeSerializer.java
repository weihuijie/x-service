package com.x.realtime.analysis.service.flink;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Kryo 自定义 LocalDateTime 序列化器（适配 Kryo 2.24.0）
 * 原理：将 LocalDateTime 转为毫秒时间戳（long 类型）序列化，反序列化时再转回 LocalDateTime
 */
public class LocalDateTimeSerializer extends Serializer<LocalDateTime> {

    // 序列化：LocalDateTime → 毫秒时间戳（long）
    @Override
    public void write(Kryo kryo, Output output, LocalDateTime localDateTime) {
        // 转为 UTC 时区的毫秒数（避免时区问题，反序列化时一致）
        long millis = localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
        output.writeLong(millis); // Kryo 原生支持 long 序列化
    }

    // 反序列化：毫秒时间戳（long）→ LocalDateTime
    @Override
    public LocalDateTime read(Kryo kryo, Input input, Class<LocalDateTime> type) {
        long millis = input.readLong();
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneOffset.UTC);
    }
}