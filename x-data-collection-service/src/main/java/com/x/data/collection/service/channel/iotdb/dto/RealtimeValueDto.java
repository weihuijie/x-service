package com.x.data.collection.service.channel.iotdb.dto;

import lombok.Data;

@Data
public class RealtimeValueDto {

    /**
     * 时间戳
     */
    private long ts;

    /**
     * 时间序列
     */
    private String timeSeries;

    /**
     * 值
     */
    private Object val;
}
