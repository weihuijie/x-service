package com.x.data.collection.service.plc.read;

import com.x.data.collection.service.plc.CuBaseResponse;
import lombok.*;

import java.util.List;
import java.util.Map;

/**
  plc读取结果
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString
public class PlcReadRes extends CuBaseResponse<PlcReadRes.PlcReadData> {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PlcReadData {
        private Map<String, Object> simpleValues;

        private Map<String, List<Object>> customValues;
    }
}
