package com.x.data.collection.service.utils.plc.write;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlcWriteReq {

    private String reqId;

    private String dataAddr;

    private List<PlcWriteSimplePoint> simplePoints;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PlcWriteSimplePoint {
        private String targetValue;

        private String valueType;

        private String addr;

        private String optType = "DirectWrite";
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PlcWriteCustomPoint {
        private String [] targetValues;

        private String valueType;

        private String name;

        private String startPos;
    }
}
