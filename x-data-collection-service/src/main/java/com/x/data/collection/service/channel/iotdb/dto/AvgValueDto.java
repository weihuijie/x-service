package com.x.data.collection.service.channel.iotdb.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AvgValueDto {

    /**
     * 测点
     */
    private String point;

    /**
     * 平均值
     */
    private Double val;

}
