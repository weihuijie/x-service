package com.x.realtime.analysis.service.flink;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.serialization.SerializationSchema;

import java.nio.charset.StandardCharsets;

/**
 * SensorData到RabbitMQ消息的序列化器
 */
public class SensorDataRMQSerializationSchema implements SerializationSchema<SensorData> {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public byte[] serialize(SensorData sensorData) {
        try {
            // 将SensorData对象转换为JSON字符串，再转换为字节数组
            String json = objectMapper.writeValueAsString(sensorData);
            return json.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("序列化SensorData到RabbitMQ消息时出错: " + e.getMessage());
            e.printStackTrace();
            return new byte[0];
        }
    }
}