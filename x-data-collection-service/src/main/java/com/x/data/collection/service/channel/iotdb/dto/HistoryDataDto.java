package com.x.data.collection.service.channel.iotdb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistoryDataDto {

    /**
     * 测点
     */
    private String point;

    /**
     * 平均值
     */
    private List<HistoryValueDto> values;
}
