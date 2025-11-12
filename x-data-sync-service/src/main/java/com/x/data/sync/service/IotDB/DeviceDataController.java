package com.x.data.sync.service.IotDB;

import com.x.common.base.R;
import com.x.repository.service.entity.DeviceInfoEntity;
import com.x.repository.service.entity.DevicePointInfoEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * IotDB 数据读写
 *
 * @author whj
 */
@RestController
@RequestMapping("/data/sync")
@RequiredArgsConstructor
public class DeviceDataController {
    private final DeviceDataService deviceDataService;

    @PostMapping
    public R<Object> write(@RequestBody List<DeviceInfoEntity> data) {
        deviceDataService.writeDeviceData(data);
        return R.success("写入成功"); // Spring Boot Plus 统一响应
    }

    @GetMapping("/latest")
    public R<DevicePointInfoEntity> getLatest(
            @RequestParam("deviceCode") String deviceCode,
            @RequestParam("pointId") Long pointId
    ) {
        DevicePointInfoEntity devicePointData;
        try {
            devicePointData = deviceDataService.queryLatest(deviceCode, pointId);
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
        return R.data(devicePointData);
    }

    @GetMapping("/history")
    public R<List<DevicePointInfoEntity>> getHistory(
            @RequestParam("deviceCode") String deviceCode,
            @RequestParam("pointId") Long pointId,
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime
    ) {
        List<DevicePointInfoEntity> devicePointDatas;
        try {
            devicePointDatas = deviceDataService.queryHistory(deviceCode, pointId, startTime, endTime);
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
        return R.data(devicePointDatas);
    }
}