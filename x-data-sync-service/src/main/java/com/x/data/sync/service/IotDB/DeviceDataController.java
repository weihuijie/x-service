package com.x.data.sync.service.IotDB;

import com.x.common.base.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * IotDB 数据读写
 *
 * @author whj
 */
@RestController
@RequestMapping("/api/device-data")
@RequiredArgsConstructor
public class DeviceDataController {
    private final DeviceDataService deviceDataService;

    @PostMapping
    public R<Object> write(@RequestBody DevicePointData data) {
        deviceDataService.writeData(data);
        return R.success("写入成功"); // Spring Boot Plus 统一响应
    }

    @GetMapping("/latest")
    public R<DevicePointData> getLatest(
            @RequestParam("deviceId") Long deviceId,
            @RequestParam("pointId") Long pointId
    ) {
        DevicePointData devicePointData;
        try {
            devicePointData = deviceDataService.queryLatest(deviceId, pointId);
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
        return R.data(devicePointData);
    }

    @GetMapping("/history")
    public R<List<DevicePointData>> getHistory(
            @RequestParam("deviceId") Long deviceId,
            @RequestParam("pointId") Long pointId,
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime
    ) {
        List<DevicePointData> devicePointDatas;
        try {
            devicePointDatas = deviceDataService.queryHistory(deviceId, pointId, startTime, endTime);
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
        return R.data(devicePointDatas);
    }
}