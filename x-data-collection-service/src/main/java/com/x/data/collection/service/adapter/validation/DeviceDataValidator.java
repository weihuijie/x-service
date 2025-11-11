package com.x.data.collection.service.adapter.validation;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 设备数据校验器
 * 用于校验设备推送的数据格式和内容
 *
 * @author whj
 */
@Slf4j
@Component
public class DeviceDataValidator {

    /**
     * 校验单个设备数据
     *
     * @param deviceCode 设备编码
     * @param data       数据内容
     * @return 校验结果
     */
    public ValidationResult validateDeviceData(String deviceCode, Object data) {
        ValidationResult result = new ValidationResult();
        
        // 检查设备编码
        if (!StringUtils.hasText(deviceCode)) {
            result.setValid(false);
            result.setErrorMessage("设备编码不能为空");
            return result;
        }
        
        // 检查数据内容
        if (ObjectUtils.isEmpty(data)) {
            result.setValid(false);
            result.setErrorMessage("数据内容不能为空");
            return result;
        }
        
        try {
            // 解析JSON数据
            List<JSONObject> jsonObjects = JSONArray.parseArray(JSONObject.toJSONString(data), JSONObject.class);
            AtomicBoolean access = new AtomicBoolean(true);
            // 校验字段
            jsonObjects.stream().filter(item -> {
                // 检查必要字段
                if (access.get() && !item.containsKey("timestamp")) {
                    access.set(false);
                }
                return false;
            });
            if (!access.get()) {
                result.setValid(false);
                result.setErrorMessage("数据格式不正确，缺少必要字段");
                return result;
            }

        } catch (Exception e) {
            log.error("解析设备数据时发生错误: deviceCode={}, data={}", deviceCode, data, e);
            result.setValid(false);
            result.setErrorMessage("数据格式不正确，无法解析为JSON:");
            return result;
        }
        result.setValid(true);
        return result;
    }
}