package com.x.data.sync.service.IotDB;

import com.x.repository.service.entity.DevicePointInfoEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 设备点数据（含时间戳）
 *
 * @author whj
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DevicePointData extends DevicePointInfoEntity {
    private Long timestamp; // 时序数据时间戳（必需，毫秒级）
}