package com.x.data.collection.service.schedule;

import com.x.data.collection.service.collector.DevicePointDataCollectService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DevicePointDataCollectTask {

    private final DevicePointDataCollectService devicePointDataCollectService;

    public DevicePointDataCollectTask(DevicePointDataCollectService devicePointDataCollectService) {
        this.devicePointDataCollectService = devicePointDataCollectService;
    }

    @XxlJob("DevicePointDataCollect")
    public void collect() throws Exception {
        devicePointDataCollectService.devicePointDataCollect();
    }
}
