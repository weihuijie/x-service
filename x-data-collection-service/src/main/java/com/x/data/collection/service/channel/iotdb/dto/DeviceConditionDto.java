package com.x.data.collection.service.channel.iotdb.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DeviceConditionDto {

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 时间
     */
    private Long timestamp;


    private List<Point> points;



    @Data
    @Builder
    public static class Point {
        /**
         * 点位名称
         */
        private String name;

        /**
         * 点位值
         */
        private Object value;
    }

}
