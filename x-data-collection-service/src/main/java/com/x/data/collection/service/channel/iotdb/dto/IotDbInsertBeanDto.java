package com.x.data.collection.service.channel.iotdb.dto;

import lombok.Data;
import org.apache.iotdb.tsfile.file.metadata.enums.TSDataType;

import java.util.List;

@Data
public class IotDbInsertBeanDto {

    /**
     * 设备id
     */
    private String deviceId;

    /**
     * 数据json
     */
    private String dataJson;

    /**
     * 测点列表
     */
    private List<String> pointArr;

    /**
     * 数据类型列表
     */
    private List<TSDataType> types;

}
