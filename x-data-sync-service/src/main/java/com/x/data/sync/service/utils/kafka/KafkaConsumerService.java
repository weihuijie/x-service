package com.x.data.sync.service.utils.kafka;

import com.alibaba.fastjson2.JSONObject;
import com.x.data.sync.service.IotDB.DeviceDataService;
import com.x.repository.service.entity.DeviceInfoEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Kafka 消费者服务
 *
 * @author whj
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumerService implements ConsumerSeekAware {

    private final DeviceDataService deviceDataService;

    // 分批写入IotDB的最大条数（从配置文件读取）
    @Value("${kafka.consumer.write-batch-size:500}")
    private int writeBatchSize;

    /**
     * 批量消费核心方法（引用独立配置的容器工厂）
     */
    @KafkaListener(
            topics = "${kafka.consumer.topic:test-topic}",
            groupId = "${kafka.consumer.group-id:data-sync-group}",
            containerFactory = "batchKafkaListenerContainerFactory" // 引用配置类中的工厂Bean
    )
    public void batchConsume(List<ConsumerRecord<String, String>> records, Acknowledgment acknowledgment) {
        log.debug("【开始批量消费】本次消费消息数：{}", records.size());
        long startTime = System.currentTimeMillis();

        if (records.isEmpty()) {
            acknowledgment.acknowledge(); // 空批次直接提交偏移量
            return;
        }

        // 保存原始消息Key（用于异常日志）
        List<String> recordKeys = records.stream()
                .map(ConsumerRecord::key)
                .collect(Collectors.toList());

        try {
            log.debug("【批量消费详情】size：{}",records.size());
            List<DeviceInfoEntity> allDeviceData = records
                    .stream()
                    .map(record -> JSONObject.parseObject(record.value(), DeviceInfoEntity.class))
                    .toList();

            writeToIotDB(allDeviceData);
            // 所有数据处理成功，提交偏移量（原子提交）
            acknowledgment.acknowledge();
            log.info("【批量消费成功】耗时：{}ms", System.currentTimeMillis() - startTime);
            log.debug("【批量消费成功】消息Keys：{}，已提交偏移量", recordKeys);

        } catch (Exception e) {
            log.error("【批量消费失败】消息Keys：{}，处理异常", recordKeys, e);
            // 不提交偏移量，触发重试（需配合配置类中的错误处理器）
            throw new RuntimeException("批量消费业务处理失败，等待重试", e);
        }
    }

    /**
     * 分批写入IotDB
     */
    private void writeToIotDB(List<DeviceInfoEntity> allDeviceData) {
        int totalSize = allDeviceData.size();
        int batchCount = (totalSize + writeBatchSize - 1) / writeBatchSize; // 向上取整计算分批次数

        log.debug("【开始分批写入】总条数：{}，分批次数：{}，每批大小：{}",
                totalSize, batchCount, writeBatchSize);

        for (int i = 0; i < batchCount; i++) {
            int startIndex = i * writeBatchSize;
            int endIndex = Math.min((i + 1) * writeBatchSize, totalSize);
            List<DeviceInfoEntity> batchData = allDeviceData.subList(startIndex, endIndex);

            try {
                log.debug("【写入第{}批】条数：{}", i + 1, batchData.size());
                deviceDataService.writeDeviceData(batchData);
            } catch (Exception e) {
                log.error("【第{}批写入失败】条数：{}", i + 1, batchData.size(), e);
                throw new RuntimeException("分批写入IotDB失败", e); // 触发整体重试
            }
        }

        log.debug("【所有批次写入成功】总条数：{}", totalSize);
    }
}