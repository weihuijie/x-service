package com.x.data.collection.service.channel.iotdb.dto;

import lombok.Data;

import java.util.List;

@Data
public class BatchRealtimeValueDto {

    String deviceId;

    List<RealtimeValueDto> realtimeData;
}
