package com.x.realtime.analysis.service.flink;

import com.alibaba.fastjson2.JSONObject;
import com.x.repository.service.entity.DevicePointInfoEntity;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 传感器数据序列化/反序列化类
 */
public class DevicePointInfoEntitySchema implements SerializationSchema<DevicePointInfoEntity>, DeserializationSchema<DevicePointInfoEntity> {

    @Override
    public DevicePointInfoEntity deserialize(byte[] message) throws IOException {
        String rawData = new String(message, StandardCharsets.UTF_8);
        try {
            return JSONObject.parseObject(rawData, DevicePointInfoEntity.class);
        } catch (Exception e) {
            System.err.println("Failed to deserialize sensor data: " + rawData);
        }
        return null;
    }

    @Override
    public boolean isEndOfStream(DevicePointInfoEntity nextElement) {
        return false;
    }

    @Override
    public TypeInformation<DevicePointInfoEntity> getProducedType() {
        return Types.POJO(DevicePointInfoEntity.class);
    }

    @Override
    public byte[] serialize(DevicePointInfoEntity element) {
        return JSONObject.toJSONString(element).getBytes(StandardCharsets.UTF_8);
    }
}