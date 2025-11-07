package com.x.data.collection.service.utils.plc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * plc服务状态
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlcStatus {

    private Boolean enabledCache;

    private List<PlcStatusRes> serviceStatus;
}
