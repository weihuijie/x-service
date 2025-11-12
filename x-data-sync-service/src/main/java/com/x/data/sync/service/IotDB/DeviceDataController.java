package com.x.data.sync.service.IotDB;

import com.x.common.base.R;
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
    public R<Object> write(@RequestBody DevicePointInfoEntity data) {
        deviceDataService.writeData(data);
        return R.success("写入成功"); // Spring Boot Plus 统一响应
    }

    @GetMapping("/latest")
    public R<DevicePointInfoEntity> getLatest(
            @RequestParam("deviceId") Long deviceId,
            @RequestParam("pointId") Long pointId
    ) {
        DevicePointInfoEntity devicePointData;
        try {
            devicePointData = deviceDataService.queryLatest(deviceId, pointId);
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
        return R.data(devicePointData);
    }

    @GetMapping("/history")
    public R<List<DevicePointInfoEntity>> getHistory(
            @RequestParam("deviceId") Long deviceId,
            @RequestParam("pointId") Long pointId,
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime
    ) {
        List<DevicePointInfoEntity> devicePointDatas;
        try {
            devicePointDatas = deviceDataService.queryHistory(deviceId, pointId, startTime, endTime);
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
        return R.data(devicePointDatas);
    }
}