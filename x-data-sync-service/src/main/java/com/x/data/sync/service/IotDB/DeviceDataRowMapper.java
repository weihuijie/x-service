package com.x.data.sync.service.IotDB;

import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 复用性 RowMapper，适配 IotDB 2.0.5，通过列别名 metric_val 获取指标值
 *
 * @author whj
 */
public class DeviceDataRowMapper implements RowMapper<DevicePointData> {
    private final Long deviceId;
    private final Long pointId; // 原始指标名（如 DBD1000.DB100）

    // 构造器传入 deviceId 和 pointId，用于结果封装和异常提示
    public DeviceDataRowMapper(Long deviceId, Long pointId) {
        this.deviceId = deviceId;
        this.pointId = pointId;
    }

    @Override
    public DevicePointData mapRow(ResultSet rs, int rowNum) throws SQLException {
        DevicePointData data = new DevicePointData();
        data.setDeviceId(deviceId);
        data.setId(pointId); // 存储原始指标名，符合业务使用习惯

        // 1. 时间戳：IotDB 2.0.5 时间戳为 LONG 类型，直接获取（避免空值）
        long timestamp = rs.getLong("Time");
        data.setTimestamp(timestamp);

        // 2. 指标值：通过别名 metric_val 获取，避免复杂列名解析错误
        Object value = rs.getObject("metric_val");
        // 空值处理：根据业务决定是否允许 NULL（此处抛异常，可按需调整）
        if (value == null) {
            throw new SQLException(
                String.format(
                    "历史数据空值异常：deviceId=%d, pointId=%s, 时间戳=%d, 行号=%d",
                    deviceId, pointId, timestamp, rowNum
                )
            );
        }
        data.setPointValue(value);
        return data;
    }
}