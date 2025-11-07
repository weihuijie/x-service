package com.x.data.collection.service.collector;

import com.alibaba.fastjson2.JSONObject;
import com.x.data.collection.service.utils.kafka.KafkaProducerService;
import com.x.data.collection.service.utils.plc.PlcService;
import com.x.data.collection.service.utils.plc.PlcValueType;
import com.x.data.collection.service.utils.plc.read.PlcReadDto;
import com.x.dubbo.api.device.IDeviceInfoDubboService;
import com.x.repository.service.entity.DeviceInfoEntity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DevicePointDataCollectService {

    @DubboReference(version = "1.0.0")
    private IDeviceInfoDubboService deviceInfoDubboService;

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
            List<DeviceCollectPoint> deviceCollectPoints = getDeviceCollectPoint(deviceInfoEntity.getId());
            // 构建 PLC 读取请求
            List<PlcReadDto<Object>> plcReadDtoList = deviceCollectPoints.stream()
                    .map(e -> PlcReadDto.build(e.getPointAddr(), PlcValueType.getByCode(e.getPointType()))).filter(Objects::nonNull).toList();
            // 发送 PLC 请求
            List<PlcReadDto<Object>> plcData = plcService.getPlcData(plcReadDtoList, deviceInfoEntity.getPlcCode(), 1000);
            // 将结果转成map ， key = pointAddr, value = pointValue
            Map<String, Object> collectMap = plcData.stream().collect(Collectors.toMap(PlcReadDto::getAddress, PlcReadDto::getValue, (o, n) -> n));
            // 赋值给点位信息
            List<DeviceCollectPoint> sendData = new ArrayList<>();
            for (DeviceCollectPoint deviceCollectPoint : deviceCollectPoints) {
                if (collectMap.get(deviceCollectPoint.getPointAddr()) == null){
                    continue;
                }
                deviceCollectPoint.setPointValue(collectMap.get(deviceCollectPoint.getPointAddr()));
                sendData.add(deviceCollectPoint);
            }
            log.info("设备：{}，点位信息：{}", deviceInfoEntity.getDeviceName(), deviceCollectPoints);
            kafkaProducerService.sendMessageAsync(deviceInfoEntity.getDeviceCode(), JSONObject.toJSONString(sendData));

        }
    }


    /**
     *  demo 测试数据
     */
    private static  List<DeviceCollectPoint> getDeviceCollectPoint(Long deviceId){

        List<DeviceCollectPoint> deviceCollectPoints = new ArrayList<>();
        int num = 10;
        for (int i = 0; i < num; i++) {
            DeviceCollectPoint deviceCollectPoint = new DeviceCollectPoint();
            deviceCollectPoint.setId(Integer.toUnsignedLong(i));
            deviceCollectPoint.setDeviceId(deviceId);
            Random random = new Random();
            deviceCollectPoint.setPointType(random.nextInt(6));

            String pointAddr = "DB3000";
            if (Objects.equals(deviceCollectPoint.getPointType(), PlcValueType.BOOL.getCode())){
                pointAddr = pointAddr + "." + "DBX"+random.nextInt(num)+".0";
            }else {
                pointAddr = pointAddr + "." + "DBD"+random.nextInt(num*100);
            }
            deviceCollectPoint.setPointAddr(pointAddr);
            deviceCollectPoints.add(deviceCollectPoint);
        }

        return deviceCollectPoints;
    }
    @Data
    static class DeviceCollectPoint{

        private Long id;
        private Long deviceId;
        private String pointAddr;
        private Integer pointType;
        private Object pointValue;

    }
}
