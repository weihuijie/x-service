package com.x.realtime.analysis.service.flink;

import com.alibaba.fastjson2.JSONObject;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 传感器数据序列化/反序列化类
 */
public class SensorDataSchema implements SerializationSchema<SensorData>, DeserializationSchema<SensorData> {

    @Override
    public SensorData deserialize(byte[] message) throws IOException {
        String rawData = new String(message, StandardCharsets.UTF_8);
        try {
            return JSONObject.parseObject(rawData, SensorData.class);
        } catch (Exception e) {
            System.err.println("Failed to deserialize sensor data: " + rawData);
        }
        return null;
    }

    @Override
    public boolean isEndOfStream(SensorData nextElement) {
        return false;
    }

    @Override
    public TypeInformation<SensorData> getProducedType() {
        return Types.POJO(SensorData.class);
    }

    @Override
    public byte[] serialize(SensorData element) {
        return JSONObject.toJSONString(element).getBytes(StandardCharsets.UTF_8);
    }
}