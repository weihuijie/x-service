package com.x.data.collection.service.channel.iotdb.vo;

import lombok.Data;

import java.util.List;

@Data
public class IotDbBatchHistoryRequestQueryVo {

    /**
     * 设备完整的树路径
     */
    private List<String> deviceIds;

    /**
     * 测点编码
     */
    private List<String> points;

    /**
     * 开始时间
     */
    private Long startTime;

    /**
     * 结束时间
     */
    private Long endTime;

    /**
     * 分页
     */
    private Integer page;

    /**
     * 页大小
     */
    private Integer pageSize;

    /**
     * (窗口大小)
     */
    private Integer windowSize;

    /**
     * 单位 天/毫秒/秒 d/ms/s
     */
    private String interval;

}
