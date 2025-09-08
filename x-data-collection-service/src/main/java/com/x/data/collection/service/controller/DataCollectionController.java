package com.x.data.collection.service.controller;

import com.x.data.collection.service.channel.kafka.DataCollectorForKafka;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/data")
public class DataCollectionController {

    @Autowired
    private DataCollectorForKafka dataCollectorForKafka;

    // 接收设备数据
    @PostMapping("/device/{deviceId}")
    public ResponseEntity<?> receiveDeviceData(
            @PathVariable String deviceId,
            @RequestBody DeviceDataRequest request) {

        try {
            // 处理设备数据
            boolean processed = dataCollectorForKafka.processDeviceData(deviceId, request.getData());

            if (processed) {
                // 清洗数据
                DataCollectorForKafka.ProcessedData processedData = dataCollectorForKafka.cleanData(request.getData());

                // 存储到IoTDB
                boolean stored = dataCollectorForKafka.storeToIoTDB(processedData);

                if (stored) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("message", "Data collected and stored successfully");
                    response.put("dataId", processedData.getId());
                    return ResponseEntity.ok(response);
                } else {
                    return ResponseEntity.status(500).body("Failed to store data");
                }
            } else {
                return ResponseEntity.status(500).body("Failed to process data");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error processing data: " + e.getMessage());
        }
    }

    // 获取数据收集统计信息
    @GetMapping("/stats")
    public ResponseEntity<?> getCollectionStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDataPoints", dataCollectorForKafka.getTotalDataPoints());

        Map<String, Long> deviceStats = new HashMap<>();
        // 在实际应用中，这里会返回所有设备的统计数据
        stats.put("deviceStats", deviceStats);

        return ResponseEntity.ok(stats);
    }

    // 设备数据请求类
    public static class DeviceDataRequest {
        private String data;

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }
}
