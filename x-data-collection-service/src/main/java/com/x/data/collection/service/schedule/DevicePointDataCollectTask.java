package com.x.data.collection.service.schedule;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DevicePointDataCollectTask {


    @XxlJob("DevicePointDataCollect")
    public void collect() throws Exception {
        XxlJobHelper.log("DevicePointDataCollectTask start");
        log.info("DevicePointDataCollectTask start");
    }
}
