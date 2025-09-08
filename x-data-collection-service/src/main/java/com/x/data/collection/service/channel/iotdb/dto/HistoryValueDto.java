package com.x.data.collection.service.channel.iotdb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistoryValueDto {

    /**
     * 值
     */
    private Object val;

    /**
     * 时间戳
     */
    private long timestamp;

}
