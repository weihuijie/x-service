package com.x.realtime.analysis.service.flink;

import lombok.Data;

/**
 * 传感器数据实体类
 */
@Data
public class SensorData {
    private Long id;
    private Long deviceId;
    private String pointAddr;
    private Integer pointType;
    private Object pointValue;
}
