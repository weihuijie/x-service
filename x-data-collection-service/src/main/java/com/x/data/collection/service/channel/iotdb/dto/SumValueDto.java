package com.x.data.collection.service.channel.iotdb.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SumValueDto {

    /**
     * 测点
     */
    private String point;

    /**
     * 求和值
     */
    private Double val;

}
