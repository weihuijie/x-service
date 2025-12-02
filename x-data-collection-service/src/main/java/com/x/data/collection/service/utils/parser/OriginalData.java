package com.x.data.collection.service.utils.parser;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OriginalData {
    private Integer temperature; // 温度（对应原始数据"温度"）
    private Integer humidity;    // 湿度（对应原始数据"湿度"）
    private Integer speed;       // 转速（对应原始数据"转速"）
    private Boolean startStatus; // 启动状态（对应原始数据"启动状态"）
    private Integer pressure;    // 压力（对应原始数据"压力"）
    private BigDecimal weight;   // 重量（对应原始数据"重量"）
}