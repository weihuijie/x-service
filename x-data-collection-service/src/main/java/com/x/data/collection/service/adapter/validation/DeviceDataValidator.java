package com.x.data.collection.service.adapter.validation;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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

        result.setValid(true);
        return result;
    }
}