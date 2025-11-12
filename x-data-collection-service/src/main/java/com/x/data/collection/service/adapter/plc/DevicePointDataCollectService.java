package com.x.data.collection.service.adapter.plc;

import com.alibaba.fastjson2.JSONObject;
import com.x.data.collection.service.utils.kafka.KafkaProducerService;
import com.x.data.collection.service.utils.plc.PlcService;
import com.x.data.collection.service.utils.plc.PlcValueType;
import com.x.data.collection.service.utils.plc.read.PlcReadDto;
import com.x.dubbo.api.device.IDeviceInfoDubboService;
import com.x.dubbo.api.device.IDevicePointInfoDubboService;
import com.x.repository.service.entity.DeviceInfoEntity;
import com.x.repository.service.entity.DevicePointInfoEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
/**
 * 设备点位数据采集服务
 *
 *   @author whj
 */
@Slf4j
@Component
public class DevicePointDataCollectService {

    @DubboReference(version = "1.0.0")
    private IDeviceInfoDubboService deviceInfoDubboService;

    @DubboReference(version = "1.0.0")
    private IDevicePointInfoDubboService devicePointInfoDubboService;

    @Autowired
    private PlcService plcService;

    @Autowired
    private KafkaProducerService kafkaProducerService;


    public void devicePointDataCollect() {
        // 获取所有设备
        List<DeviceInfoEntity> list = deviceInfoDubboService.list(new DeviceInfoEntity()).getData();
        log.info("设备数量：{}", list.size());
        for (DeviceInfoEntity deviceInfoEntity : list) {
            // 获取点位信息
            List<DevicePointInfoEntity> deviceCollectPoints = getDeviceCollectPoint(deviceInfoEntity.getId());
            // 构建 PLC 读取请求
            List<PlcReadDto<Object>> plcReadDtoList = deviceCollectPoints.stream()
                    .map(e -> PlcReadDto.build(e.getPointAddr(), PlcValueType.getByCode(e.getPointType()))).filter(Objects::nonNull).toList();
            // 发送 PLC 请求
            List<PlcReadDto<Object>> plcData = plcService.getPlcData(plcReadDtoList, deviceInfoEntity.getPlcCode(), 1000);
            // 时间戳
            Long timestamp = System.currentTimeMillis();
            // 将结果转成map ， key = pointAddr, value = pointValue
            Map<String, Object> collectMap = plcData.stream().collect(Collectors.toMap(PlcReadDto::getAddress, PlcReadDto::getValue, (o, n) -> n));
            // 赋值给点位信息
            List<DevicePointInfoEntity> sendData = new ArrayList<>();
            for (DevicePointInfoEntity deviceCollectPoint : deviceCollectPoints) {
                if (collectMap.get(deviceCollectPoint.getPointAddr()) == null){
                    continue;
                }
                deviceCollectPoint.setPointValue(collectMap.get(deviceCollectPoint.getPointAddr()));
                deviceCollectPoint.setTimestamp(timestamp);
                sendData.add(deviceCollectPoint);
            }
            deviceInfoEntity.setPointList(deviceCollectPoints);
            log.info("设备：{}，点位信息：{}", deviceInfoEntity.getDeviceName(), deviceCollectPoints);
            kafkaProducerService.sendMessageAsync(deviceInfoEntity.getDeviceCode(), JSONObject.toJSONString(deviceInfoEntity));
        }
    }


    /**
     *  demo 测试数据
     */
    private  List<DevicePointInfoEntity> getDeviceCollectPoint(Long deviceId){
        DevicePointInfoEntity devicePointInfoEntity = new DevicePointInfoEntity();
        devicePointInfoEntity.setDeviceId(deviceId);

        return devicePointInfoDubboService.list(devicePointInfoEntity).getData();
    }
}
