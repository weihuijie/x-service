package com.x.data.collection.service.channel.kafka;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.x.data.collection.service.channel.iotdb.service.IotDBService;
import com.x.data.collection.service.channel.mysql.DataValidationAndStorageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.iotdb.tsfile.file.metadata.enums.TSDataType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 高性能数据收集器
 * 用于处理大规模设备数据收集和存储
 */
@Slf4j
@Component
public class HighPerformanceDataCollector {

    @Autowired
    private IotDBService iotDBService;
    
    @Autowired
    private DataValidationAndStorageService dataValidationService;

    // 数据处理线程池
    private ExecutorService dataProcessExecutor;
    
    // 批量处理队列
    private final BlockingQueue<DeviceDataBatch> dataBatchQueue = new LinkedBlockingQueue<>(1000);
    
    // 批处理大小
    private static final int BATCH_SIZE = 1000;
    
    // 批处理时间间隔(毫秒)
    private static final int BATCH_INTERVAL_MS = 1000;
    
    // 统计信息
    private final AtomicLong totalProcessedPoints = new AtomicLong(0);
    private final AtomicLong totalProcessedDevices = new AtomicLong(0);
    
    // 批处理工作线程
    private volatile boolean running = true;
    private Thread batchProcessorThread;

    @PostConstruct
    public void init() {
        // 初始化线程池
        int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;
        dataProcessExecutor = new ThreadPoolExecutor(
                corePoolSize,
                corePoolSize * 2,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                new ThreadFactory() {
                    private final AtomicLong threadNumber = new AtomicLong(1);
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r, "data-process-thread-" + threadNumber.getAndIncrement());
                        t.setDaemon(false);
                        return t;
                    }
                }
        );
        
        // 启动批处理线程
        batchProcessorThread = new Thread(this::batchProcessData, "batch-processor-thread");
        batchProcessorThread.setDaemon(false);
        batchProcessorThread.start();
        
        log.info("HighPerformanceDataCollector initialized with {} core threads", corePoolSize);
    }

    @PreDestroy
    public void destroy() {
        running = false;
        if (batchProcessorThread != null) {
            batchProcessorThread.interrupt();
        }
        if (dataProcessExecutor != null) {
            dataProcessExecutor.shutdown();
            try {
                if (!dataProcessExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                    dataProcessExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                dataProcessExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        log.info("HighPerformanceDataCollector destroyed");
    }

    /**
     * 收集设备数据并进行批量处理
     * @param deviceId 设备ID
     * @param jsonData 设备数据，JSON格式
     */
    public void collectDeviceData(String deviceId, String jsonData) {
        try {
            // 解析JSON数据
            JSONObject jsonObject = JSON.parseObject(jsonData);
            JSONArray dataArray = jsonObject.getJSONArray("data");
            
            if (dataArray != null && !dataArray.isEmpty()) {
                // 创建数据批次
                DeviceDataBatch batch = new DeviceDataBatch(deviceId, dataArray);
                
                // 添加到批处理队列
                if (!dataBatchQueue.offer(batch)) {
                    log.warn("Data batch queue is full, dropping data for device: {}", deviceId);
                }
            }
        } catch (Exception e) {
            log.error("Error parsing device data for device: {}", deviceId, e);
        }
    }

    /**
     * 批量处理数据
     */
    private void batchProcessData() {
        List<DeviceDataBatch> batchBuffer = new ArrayList<>(BATCH_SIZE);
        
        while (running) {
            try {
                batchBuffer.clear();
                
                // 获取第一批数据
                DeviceDataBatch firstBatch = dataBatchQueue.poll(BATCH_INTERVAL_MS, TimeUnit.MILLISECONDS);
                if (firstBatch != null) {
                    batchBuffer.add(firstBatch);
                    
                    // 尝试获取更多数据以组成一个批次
                    dataBatchQueue.drainTo(batchBuffer, BATCH_SIZE - 1);
                } else {
                    // 超时，继续下一轮循环
                    continue;
                }
                
                // 提交批次处理任务
                dataProcessExecutor.submit(() -> processBatch(batchBuffer));
                
            } catch (InterruptedException e) {
                log.info("Batch processor thread interrupted");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error in batch processing", e);
            }
        }
    }

    /**
     * 处理数据批次
     * @param batchBuffer 数据批次列表
     */
    private void processBatch(List<DeviceDataBatch> batchBuffer) {
        try {
            long startTime = System.currentTimeMillis();
            int totalPoints = 0;
            
            for (DeviceDataBatch batch : batchBuffer) {
                // 处理单个设备数据批次
                int points = processDeviceBatch(batch);
                totalPoints += points;
                totalProcessedDevices.incrementAndGet();
            }
            
            long endTime = System.currentTimeMillis();
            totalProcessedPoints.addAndGet(totalPoints);
            
            log.debug("Processed batch of {} devices with {} points in {} ms", 
                    batchBuffer.size(), totalPoints, (endTime - startTime));
            
            // 定期输出统计信息
            if (totalProcessedDevices.get() % 10000 == 0) {
                log.info("Total processed devices: {}, total points: {}", 
                        totalProcessedDevices.get(), totalProcessedPoints.get());
                
                // 输出校验统计信息
                DataValidationAndStorageService.ValidationStats validationStats = dataValidationService.getStats();
                log.info("Data validation stats: {}", validationStats);
            }
        } catch (Exception e) {
            log.error("Error processing batch", e);
        }
    }

    /**
     * 处理单个设备数据批次
     * @param batch 设备数据批次
     * @return 处理的数据点数
     */
    private int processDeviceBatch(DeviceDataBatch batch) {
        try {
            String deviceId = batch.getDeviceId();
            JSONArray dataArray = batch.getDataArray();
            
            // 准备数据点信息（实际项目中可能需要从配置或数据库获取）
            List<String> measurements = new ArrayList<>();
            List<TSDataType> types = new ArrayList<>();
            
            // 简化处理：假设所有测量点都是FLOAT类型
            // 实际项目中应该根据具体配置来确定数据类型
            JSONObject sampleData = dataArray.getJSONObject(0);
            for (String key : sampleData.keySet()) {
                if (!"timestamp".equals(key)) {  // timestamp是特殊字段
                    measurements.add(key);
                    types.add(TSDataType.FLOAT);
                }
            }
            
            // 构造数据JSON
            JSONObject dataJson = new JSONObject();
            dataJson.put("data", dataArray);
            String jsonData = dataJson.toJSONString();
            
            // 插入到IoTDB
            int insertedRows = iotDBService.insertData("root.devices." + deviceId, measurements, types, jsonData);
            
            // 异步进行数据校验和MySQL存储
            validateAndStoreDataAsync(deviceId, dataArray);
            
            return insertedRows * measurements.size(); // 返回数据点数
        } catch (Exception e) {
            log.error("Error processing device batch for device: {}", batch.getDeviceId(), e);
            return 0;
        }
    }

    /**
     * 异步校验和存储数据
     * @param deviceId 设备ID
     * @param dataArray 数据数组
     */
    private void validateAndStoreDataAsync(String deviceId, JSONArray dataArray) {
        try {
            // 转换数据格式用于校验
            List<DataValidationAndStorageService.DataPoint> dataPoints = new ArrayList<>();
            
            for (int i = 0; i < dataArray.size(); i++) {
                JSONObject dataObj = dataArray.getJSONObject(i);
                long timestamp = dataObj.getLongValue("timestamp", System.currentTimeMillis());
                
                for (String key : dataObj.keySet()) {
                    if (!"timestamp".equals(key)) {
                        Object value = dataObj.get(key);
                        dataPoints.add(new DataValidationAndStorageService.DataPoint(key, value, timestamp));
                    }
                }
            }
            
            // 异步校验和存储
            dataValidationService.validateAndStoreAsync(deviceId, dataPoints);
        } catch (Exception e) {
            log.error("Error preparing data for validation and storage for device: {}", deviceId, e);
        }
    }

    /**
     * 获取处理统计信息
     */
    public DataCollectionStats getStats() {
        return new DataCollectionStats(totalProcessedDevices.get(), totalProcessedPoints.get());
    }

    /**
     * 设备数据批次类
     */
    private static class DeviceDataBatch {
        private final String deviceId;
        private final JSONArray dataArray;

        public DeviceDataBatch(String deviceId, JSONArray dataArray) {
            this.deviceId = deviceId;
            this.dataArray = dataArray;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public JSONArray getDataArray() {
            return dataArray;
        }
    }

    /**
     * 数据收集统计信息类
     */
    public static class DataCollectionStats {
        private final long totalDevices;
        private final long totalPoints;

        public DataCollectionStats(long totalDevices, long totalPoints) {
            this.totalDevices = totalDevices;
            this.totalPoints = totalPoints;
        }

        public long getTotalDevices() {
            return totalDevices;
        }

        public long getTotalPoints() {
            return totalPoints;
        }

        @Override
        public String toString() {
            return "DataCollectionStats{" +
                    "totalDevices=" + totalDevices +
                    ", totalPoints=" + totalPoints +
                    '}';
        }
    }
}