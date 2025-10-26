package com.x.data.collection.service.channel.mysql;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 数据校验和MySQL存储服务
 * 用于校验IoTDB中的数据并存储到MySQL中
 */
@Slf4j
@Service
public class DataValidationAndStorageService {

    // 数据校验和存储线程池
    private final ExecutorService validationExecutor;
    
    // 统计信息
    private final AtomicLong totalValidatedPoints = new AtomicLong(0);
    private final AtomicLong totalStoredRecords = new AtomicLong(0);
    private final AtomicLong totalValidationErrors = new AtomicLong(0);

    public DataValidationAndStorageService() {
        // 初始化线程池
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        this.validationExecutor = Executors.newFixedThreadPool(
                corePoolSize,
                new ThreadFactory() {
                    private final AtomicLong threadNumber = new AtomicLong(1);
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r, "data-validation-thread-" + threadNumber.getAndIncrement());
                        t.setDaemon(false);
                        return t;
                    }
                }
        );
    }

    /**
     * 异步校验和存储设备数据
     * @param deviceId 设备ID
     * @param dataPoints 数据点列表
     * @return CompletableFuture
     */
    public CompletableFuture<Void> validateAndStoreAsync(String deviceId, List<DataPoint> dataPoints) {
        return CompletableFuture.runAsync(() -> {
            try {
                // 校验数据
                validateDataPoints(deviceId, dataPoints);
                
                // 存储到MySQL
                storeToMySQL(deviceId, dataPoints);
                
                totalValidatedPoints.addAndGet(dataPoints.size());
                totalStoredRecords.incrementAndGet();
                
                // 定期输出统计信息
                long validated = totalValidatedPoints.get();
                if (validated % 100000 == 0) {
                    log.info("Total validated points: {}, stored records: {}, validation errors: {}", 
                            validated, totalStoredRecords.get(), totalValidationErrors.get());
                }
            } catch (Exception e) {
                log.error("Error validating and storing data for device: {}", deviceId, e);
                totalValidationErrors.incrementAndGet();
            }
        }, validationExecutor);
    }

    /**
     * 校验数据点
     * @param deviceId 设备ID
     * @param dataPoints 数据点列表
     */
    private void validateDataPoints(String deviceId, List<DataPoint> dataPoints) {
        // 实际项目中这里会进行数据校验
        // 例如：检查数值范围、时间序列连续性等
        
        for (DataPoint point : dataPoints) {
            // 示例校验逻辑
            if (point.getValue() == null) {
                log.warn("Null value detected for device: {}, point: {}, timestamp: {}", 
                        deviceId, point.getMeasurement(), point.getTimestamp());
                totalValidationErrors.incrementAndGet();
                continue;
            }
            
            // 根据测量点名称进行不同的校验
            switch (point.getMeasurement()) {
                case "temperature":
                    validateTemperature(point);
                    break;
                case "humidity":
                    validateHumidity(point);
                    break;
                case "pressure":
                    validatePressure(point);
                    break;
                default:
                    // 默认校验：检查是否为有效数值
                    validateGenericValue(point);
                    break;
            }
        }
    }

    /**
     * 校验温度数据
     * @param point 数据点
     */
    private void validateTemperature(DataPoint point) {
        try {
            double value = Double.parseDouble(point.getValue().toString());
            // 假设有效温度范围为-40到80摄氏度
            if (value < -40 || value > 80) {
                log.warn("Temperature out of range: {} for point: {}, timestamp: {}", 
                        value, point.getMeasurement(), point.getTimestamp());
                totalValidationErrors.incrementAndGet();
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid temperature value: {} for point: {}, timestamp: {}", 
                    point.getValue(), point.getMeasurement(), point.getTimestamp());
            totalValidationErrors.incrementAndGet();
        }
    }

    /**
     * 校验湿度数据
     * @param point 数据点
     */
    private void validateHumidity(DataPoint point) {
        try {
            double value = Double.parseDouble(point.getValue().toString());
            // 假设有效湿度范围为0到100%
            if (value < 0 || value > 100) {
                log.warn("Humidity out of range: {} for point: {}, timestamp: {}", 
                        value, point.getMeasurement(), point.getTimestamp());
                totalValidationErrors.incrementAndGet();
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid humidity value: {} for point: {}, timestamp: {}", 
                    point.getValue(), point.getMeasurement(), point.getTimestamp());
            totalValidationErrors.incrementAndGet();
        }
    }

    /**
     * 校验压力数据
     * @param point 数据点
     */
    private void validatePressure(DataPoint point) {
        try {
            double value = Double.parseDouble(point.getValue().toString());
            // 假设有效压力范围为0到10000kPa
            if (value < 0 || value > 10000) {
                log.warn("Pressure out of range: {} for point: {}, timestamp: {}", 
                        value, point.getMeasurement(), point.getTimestamp());
                totalValidationErrors.incrementAndGet();
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid pressure value: {} for point: {}, timestamp: {}", 
                    point.getValue(), point.getMeasurement(), point.getTimestamp());
            totalValidationErrors.incrementAndGet();
        }
    }

    /**
     * 校验通用数值数据
     * @param point 数据点
     */
    private void validateGenericValue(DataPoint point) {
        try {
            Double.parseDouble(point.getValue().toString());
        } catch (NumberFormatException e) {
            log.warn("Invalid numeric value: {} for point: {}, timestamp: {}", 
                    point.getValue(), point.getMeasurement(), point.getTimestamp());
            totalValidationErrors.incrementAndGet();
        }
    }

    /**
     * 存储数据到MySQL
     * @param deviceId 设备ID
     * @param dataPoints 数据点列表
     */
    private void storeToMySQL(String deviceId, List<DataPoint> dataPoints) {
        // 实际项目中这里会将校验后的数据存储到MySQL
        // 可以使用批量插入来提高性能
        
        // 示例：构造SQL并执行批量插入
        /*
        String sql = "INSERT INTO device_data (device_id, measurement, value, timestamp) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            for (DataPoint point : dataPoints) {
                ps.setString(1, deviceId);
                ps.setString(2, point.getMeasurement());
                ps.setObject(3, point.getValue());
                ps.setLong(4, point.getTimestamp());
                ps.addBatch();
            }
            
            ps.executeBatch();
        } catch (SQLException e) {
            log.error("Error storing data to MySQL for device: {}", deviceId, e);
            throw new RuntimeException(e);
        }
        */
        
        log.debug("Stored {} data points to MySQL for device: {}", dataPoints.size(), deviceId);
    }

    /**
     * 获取统计信息
     * @return 统计信息
     */
    public ValidationStats getStats() {
        return new ValidationStats(
                totalValidatedPoints.get(),
                totalStoredRecords.get(),
                totalValidationErrors.get()
        );
    }

    /**
     * 数据点类
     */
    public static class DataPoint {
        private String measurement;
        private Object value;
        private long timestamp;

        public DataPoint(String measurement, Object value, long timestamp) {
            this.measurement = measurement;
            this.value = value;
            this.timestamp = timestamp;
        }

        public String getMeasurement() {
            return measurement;
        }

        public Object getValue() {
            return value;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    /**
     * 校验统计信息类
     */
    public static class ValidationStats {
        private final long totalValidatedPoints;
        private final long totalStoredRecords;
        private final long totalValidationErrors;

        public ValidationStats(long totalValidatedPoints, long totalStoredRecords, long totalValidationErrors) {
            this.totalValidatedPoints = totalValidatedPoints;
            this.totalStoredRecords = totalStoredRecords;
            this.totalValidationErrors = totalValidationErrors;
        }

        public long getTotalValidatedPoints() {
            return totalValidatedPoints;
        }

        public long getTotalStoredRecords() {
            return totalStoredRecords;
        }

        public long getTotalValidationErrors() {
            return totalValidationErrors;
        }

        @Override
        public String toString() {
            return "ValidationStats{" +
                    "totalValidatedPoints=" + totalValidatedPoints +
                    ", totalStoredRecords=" + totalStoredRecords +
                    ", totalValidationErrors=" + totalValidationErrors +
                    '}';
        }
    }
}