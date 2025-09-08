package com.x.data.collection.service.channel.kafka;

import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class DataCollectorForKafka {

    // 模拟存储收集到的数据统计信息
    private final ConcurrentHashMap<String, AtomicLong> deviceDataCount = new ConcurrentHashMap<>();
    private final AtomicLong totalDataPoints = new AtomicLong(0);

    /**
     * 从Kafka收集数据
     */
    public void collectFromKafka() {
        // 实际项目中这里会连接Kafka并消费数据
        System.out.println("Collecting data from Kafka");
    }

    /**
     * 处理设备数据
     * @param deviceId 设备ID
     * @param data 数据内容
     */
    public boolean processDeviceData(String deviceId, String data) {
        // 实际项目中这里会进行数据处理

        // 记录设备数据点数量
        deviceDataCount.computeIfAbsent(deviceId, k -> new AtomicLong(0)).incrementAndGet();

        // 记录总数据点数量
        totalDataPoints.incrementAndGet();

        System.out.println("Processing data from device " + deviceId + ": " + data);
        return true;
    }

    /**
     * 清洗数据
     */
    public ProcessedData cleanData(String rawData) {
        // 实际项目中这里会进行数据清洗
        System.out.println("Cleaning raw data");

        ProcessedData processedData = new ProcessedData();
        processedData.setId(UUID.randomUUID().toString());
        processedData.setCleanedData("cleaned_" + rawData);
        processedData.setTimestamp(System.currentTimeMillis());

        return processedData;
    }

    /**
     * 存储到IoTDB
     */
    public boolean storeToIoTDB(ProcessedData data) {
        // 实际项目中这里会将数据存储到IoTDB
        System.out.println("Storing processed data to IoTDB: " + data.getCleanedData());
        return true;
    }

    /**
     * 获取设备数据统计
     */
    public long getDeviceDataCount(String deviceId) {
        AtomicLong count = deviceDataCount.get(deviceId);
        return count != null ? count.get() : 0;
    }

    /**
     * 获取总数据点数
     */
    public long getTotalDataPoints() {
        return totalDataPoints.get();
    }

    /**
     * 处理后的数据类
     */
    public static class ProcessedData {
        private String id;
        private String cleanedData;
        private long timestamp;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getCleanedData() {
            return cleanedData;
        }

        public void setCleanedData(String cleanedData) {
            this.cleanedData = cleanedData;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}
