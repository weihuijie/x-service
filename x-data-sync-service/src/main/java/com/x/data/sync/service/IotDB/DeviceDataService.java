package com.x.data.sync.service.IotDB;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 设备数据服务
 *
 * @author whj
 */
@Service
@RequiredArgsConstructor
public class DeviceDataService {
    private final DeviceDataRepository deviceDataRepository;

    // 初始化存储组（应用启动时执行）
    public void initStorageGroup() {
        deviceDataRepository.createStorageGroup();
    }

    // 写入数据（自动注册时间序列）
    @Transactional
    public void writeData(DevicePointData data) {
        deviceDataRepository.createTimeSeries(data);
        deviceDataRepository.insertData(data);
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