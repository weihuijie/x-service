package com.x.data.collection.service.collector;

import com.x.common.base.R;
import com.x.dubbo.api.device.IDeviceInfoDubboService;
import com.x.repository.service.entity.DeviceInfoEntity;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DevicePointDataCollectService {

    @DubboReference(version = "1.0.0")
    private IDeviceInfoDubboService deviceInfoDubboService;


    public void devicePointDataCollect() {
        // 获取所有设备
        R<List<DeviceInfoEntity>> list = deviceInfoDubboService.list(new DeviceInfoEntity());



    }
}
