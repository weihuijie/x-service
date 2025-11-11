package com.x.data.sync.service.IotDB;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceDataService {
    private final DeviceDataRepository deviceDataRepository;

    // 初始化存储组（应用启动时执行）
    public void initStorageGroup() {
        deviceDataRepository.createStorageGroup();
        deviceDataRepository.createTimeSeries();
    }

    // 写入数据（自动注册时间序列）
    @Transactional
    public void writeData(DevicePointData data) {
        try {
            deviceDataRepository.insertData(data);
        } catch (Exception e) {
            log.error("写入单条数据失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    // 批量写入数据
    @Transactional
    public void writeBatchData(List<DevicePointData> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return;
        }
        try {
            // 批量插入数据
            deviceDataRepository.insertBatchData(dataList);
        } catch (Exception e) {
            log.error("批量写入数据失败，数据条数: {}，错误: {}", dataList.size(), e.getMessage(), e);
            throw e;
        }
    }

    // 查询最新数据
    public DevicePointData queryLatest(Long deviceId, Long pointId) {
        return deviceDataRepository.queryLatestData(deviceId, pointId);
    }

    // 查询历史数据
    public List<DevicePointData> queryHistory(Long deviceId, Long pointId, String startTime, String endTime) {
        return deviceDataRepository.queryHistoryData(deviceId, pointId, startTime, endTime);
    }
}