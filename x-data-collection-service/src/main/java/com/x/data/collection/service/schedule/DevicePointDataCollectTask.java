package com.x.data.collection.service.schedule;

import com.x.data.collection.service.adapter.plc.DevicePointDataCollectService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
/**
 * 设备点位数据采集任务
 *
 * @author whj
 */
@Slf4j
@Component
public class DevicePointDataCollectTask {

    private final DevicePointDataCollectService devicePointDataCollectService;

    public DevicePointDataCollectTask(DevicePointDataCollectService devicePointDataCollectService) {
        this.devicePointDataCollectService = devicePointDataCollectService;
    }

    @XxlJob("DevicePointDataCollect")
    public void collect() {
        devicePointDataCollectService.devicePointDataCollect();
    }
}
