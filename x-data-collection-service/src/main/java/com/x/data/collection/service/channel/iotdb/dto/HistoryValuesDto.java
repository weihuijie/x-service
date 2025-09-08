package com.x.data.collection.service.channel.iotdb.dto;

import lombok.Data;

import java.util.List;

@Data
public class HistoryValuesDto {

    /**
     * 历史数据
     *
     * @param null
     */
    private List<HistoryDataDto> historyData;

    /**
     * 总共多少条数据
     */
    private Integer total;

    /**
     * 总共多少页
     */
    private Integer page;

    /**
     * 当前第几页
     */
    private Integer current;

    /**
     * 页大小
     */
    private Integer pageSize;

}
