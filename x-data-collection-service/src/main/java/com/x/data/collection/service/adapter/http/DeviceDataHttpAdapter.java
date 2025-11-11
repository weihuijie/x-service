package com.x.data.collection.service.adapter.http;

import com.alibaba.fastjson2.JSONObject;
import com.x.data.collection.service.adapter.validation.DeviceDataValidator;
import com.x.data.collection.service.adapter.validation.ValidationResult;
import com.x.data.collection.service.utils.kafka.KafkaProducerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * 设备数据HTTP适配器
 * 用于接收设备通过HTTP协议推送的点位数据
 *
 * @author whj
 */
@Slf4j
@RestController
@RequestMapping("/data/collection")
public class DeviceDataHttpAdapter {

    @Autowired
    private KafkaProducerService kafkaProducerService;
    
    @Autowired
    private ExecutorService deviceDataExecutor;
    
    @Autowired
    private DeviceDataValidator deviceDataValidator;

    /**
     * 接收设备推送的点位数据
     * 支持单个设备数据推送
     *
     * @param deviceCode 设备编码
     * @param data       点位数据，JSON格式
     * @return 响应结果
     */
    @PostMapping("/{deviceCode}")
    public ResponseEntity<String> receiveDeviceData(
            @PathVariable("deviceCode") String deviceCode,
            @RequestBody Object data) {
        long startTime = System.currentTimeMillis();
        // 数据校验
        ValidationResult validationResult = deviceDataValidator.validateDeviceData(deviceCode, data);
        if (!validationResult.isValid()) {
            log.warn("设备数据校验失败: deviceCode={}, error={}", deviceCode, validationResult.getErrorMessage());
            return ResponseEntity.badRequest().body("{\"error\":\"" + validationResult.getErrorMessage() + "\"}");
        }

        // 异步处理数据，提高接口响应速度
        CompletableFuture.runAsync(() -> {
            try {
                // 将数据发送到Kafka
                kafkaProducerService.sendMessageAsync(deviceCode, JSONObject.toJSONString(data));
//                log.debug("设备数据已接收并发送到Kafka: deviceCode={}, data={}", deviceCode, data);
            } catch (Exception e) {
                log.error("处理设备数据时发生错误: deviceCode={}", deviceCode, e);
            }
        }, deviceDataExecutor);

        long endTime = System.currentTimeMillis();
        log.info("处理设备数据完成: deviceCode={}, cost={}ms", deviceCode, endTime - startTime);
        // 立即返回响应，提高吞吐量
        return ResponseEntity.ok("{\"status\":\"success\"}");
    }
}