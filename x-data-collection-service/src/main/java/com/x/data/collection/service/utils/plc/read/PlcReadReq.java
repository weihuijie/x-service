package com.x.data.collection.service.utils.plc.read;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * PLC读取请求
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlcReadReq {

    private String reqId;

    private String dataAddr;

    private List<PlcSimplePoint> simplePoints;

    /**
     * plc读取点位
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PlcSimplePoint {
        private String addr;

        private String valueType;

        private String optType = "CachedRead";
    }
}
