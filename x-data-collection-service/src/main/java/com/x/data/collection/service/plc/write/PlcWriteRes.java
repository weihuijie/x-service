package com.x.data.collection.service.plc.write;

import com.x.data.collection.service.plc.CuBaseResponse;
import lombok.*;

import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class PlcWriteRes extends CuBaseResponse<PlcWriteRes.PlcWriteData> {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PlcWriteData {
        private Map<String, Boolean> simplePoints;
        private Map<String, List<Object>> customValues;
    }

}
