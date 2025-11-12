package com.x.data.sync.service.IotDB;

import com.x.repository.service.entity.DeviceInfoEntity;
import com.x.repository.service.entity.DevicePointInfoEntity;
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

    // 批量写入数据
    @Transactional
    public void writeDeviceData(List<DeviceInfoEntity> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return;
        }
        try {
            // 批量插入数据
            deviceDataRepository.insertDeviceData(dataList);
        } catch (Exception e) {
            log.error("写入数据失败，数据条数: {}，错误: {}", dataList.size(), e.getMessage(), e);
            throw e;
        }
    }

    // 查询最新数据
    public DevicePointInfoEntity queryLatest(String deviceCode, Long pointId) {
        return deviceDataRepository.queryLatestData(deviceCode, pointId);
    }

    // 查询历史数据
    public List<DevicePointInfoEntity> queryHistory(String deviceCode, Long pointId, String startTime, String endTime) {
        return deviceDataRepository.queryHistoryData(deviceCode, pointId, startTime, endTime);
    }
}