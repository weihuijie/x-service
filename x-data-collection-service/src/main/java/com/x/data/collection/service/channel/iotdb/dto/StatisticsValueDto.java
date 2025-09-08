package com.x.data.collection.service.channel.iotdb.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatisticsValueDto {

    /**
     * 测点
     */
    private String point;

    /**
     * 最大值
     */
    private String maxVal;

    /**
     * 最小值
     */
    private String minVal;

    /**
     * 平均值
     */
    private Double val;

}
