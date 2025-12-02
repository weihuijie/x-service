package com.x.data.collection.service.utils.parser;

import com.alibaba.fastjson2.JSON;
import com.x.dubbo.api.device.IDeviceInfoDubboService;
import com.x.repository.service.entity.DeviceInfoEntity;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class DeviceCacheService {
    private static final String CACHE_KEY_PREFIX = "device:info:";
    private static final long CACHE_EXPIRE_SECONDS = 3600; // 缓存1小时

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @DubboReference(version = "1.0.0")
    private IDeviceInfoDubboService deviceInfoService;

    /**
     * 根据设备编码获取缓存
     */
    public DeviceInfoEntity getDeviceInfo(String deviceCode) {
        String cacheKey = CACHE_KEY_PREFIX + deviceCode;
        // 1. 从Redis获取缓存
        String cacheValue = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cacheValue != null) {
            return JSON.parseObject(cacheValue, DeviceInfoEntity.class);
        }

        // 2. Redis未命中，查询数据库
        DeviceInfoEntity deviceInfo = deviceInfoService.infoContainsPoint(deviceCode).getData();
        if (deviceInfo == null) {
            log.error("设备编码{}未查询到设备信息", deviceCode);
            return null;
        }

        // 3. 写入Redis缓存
        stringRedisTemplate.opsForValue().set(
            cacheKey, 
            JSON.toJSONString(deviceInfo),
            CACHE_EXPIRE_SECONDS, 
            TimeUnit.SECONDS
        );
        return deviceInfo;
    }

    /**
     * 刷新设备缓存（配置变更时调用）
     */
    public void refreshDeviceCache(String deviceCode) {
        String cacheKey = CACHE_KEY_PREFIX + deviceCode;
        stringRedisTemplate.delete(cacheKey);
        log.info("设备编码{}的缓存已刷新", deviceCode);
    }
}