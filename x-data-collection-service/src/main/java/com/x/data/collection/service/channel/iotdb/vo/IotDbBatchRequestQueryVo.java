package com.x.data.collection.service.channel.iotdb.vo;

import lombok.Data;

import java.util.List;

@Data
public class IotDbBatchRequestQueryVo {

    /**
     * 数据库名称
     */
    private String db;

    /**
     * 设备完整的树路径
     */
    private List<String> deviceIds;

    /**
     * 测点编码
     */
    private List<String> points;

}
