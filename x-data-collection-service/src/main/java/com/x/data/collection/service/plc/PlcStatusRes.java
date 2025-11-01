package com.x.data.collection.service.plc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * plc服务状态
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlcStatusRes {

    private String plcName;

    @JsonProperty("deviceId")
    private String uid;

    private String plcType;

    private String location;

    private String readInfo;

    private Map<String, Boolean> instanceConnectStatus;
}
