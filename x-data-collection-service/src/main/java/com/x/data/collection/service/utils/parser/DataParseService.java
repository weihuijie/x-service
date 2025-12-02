package com.x.data.collection.service.utils.parser;

import com.alibaba.fastjson2.JSONObject;
import com.x.repository.service.entity.DeviceInfoEntity;
import com.x.repository.service.entity.DevicePointInfoEntity;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class DataParseService {
    @Resource
    private DeviceCacheService deviceCacheService;

    /**
     * 解析原始JSON数据为目标格式
     * @param originalJson 原始数据
     * @param deviceCode 设备编码
     * @return 目标格式数据
     */
    public DeviceInfoEntity parse(String originalJson, String deviceCode) {
        // 解析原始JSON为实体
        JSONObject jsonObject = JSONObject.parseObject(originalJson);
        if (jsonObject == null) {
            log.error("原始数据解析失败：{}", originalJson);
            return null;
        }

        //获取设备+点位配置
        DeviceInfoEntity deviceInfo = deviceCacheService.getDeviceInfo(deviceCode);
        if (deviceInfo == null) {
            log.error("设备信息获取失败：{}", deviceCode);
            return null;
        }
        return switch (deviceInfo.getDataCode()) {
            case "original" ->
                    // 原始数据
                    JSONObject.parseObject(originalJson, DeviceInfoEntity.class);
            case "tzos" ->
                    // JSON数据
                    parseDataTzos(jsonObject, deviceInfo);
            default -> {
                log.error("数据格式不支持：{}", deviceInfo.getDataCode());
                yield null;
            }
        };
    }

    private  static DeviceInfoEntity parseDataTzos(JSONObject jsonObject, DeviceInfoEntity deviceInfo) {
        //生成时间戳（当前时间戳，或原始数据带时间戳则用原始）
        long timestamp = System.currentTimeMillis();

        //组装pointList（核心：点位配置与原始数据值匹配）
        List<DevicePointInfoEntity> pointInfoEntityList = deviceInfo.getPointList().stream().peek(point -> {
            // 动态值从原始数据提取（通过映射快速获取）
            point.setPointValue(jsonObject.get(point.getPointName()));
            point.setTimestamp(timestamp);
        }).toList();

        //组装目标数据
        deviceInfo.setPointList(pointInfoEntityList);
        deviceInfo.setTimestamp(timestamp);

        return deviceInfo;
    }
}