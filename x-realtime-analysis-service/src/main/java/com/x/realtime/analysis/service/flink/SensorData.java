package com.x.realtime.analysis.service.flink;

import lombok.Data;

import java.io.Serializable;

/**
 * 传感器数据实体类
 */
@Data
public class SensorData implements Serializable {
    private Long id;
    private Long deviceId;
    private String pointAddr;
    private Integer pointType;
    private Object pointValue;
}
