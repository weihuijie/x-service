package com.x.data.collection.service.utils.plc;

import com.x.data.collection.service.utils.plc.read.PlcReadDto;
import com.x.data.collection.service.utils.plc.read.PlcReadReq;
import com.x.data.collection.service.utils.plc.read.PlcReadRes;
import com.x.data.collection.service.utils.plc.write.PlcWriteDto;
import com.x.data.collection.service.utils.plc.write.PlcWriteReq;
import com.x.data.collection.service.utils.plc.write.PlcWriteRes;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PlcService {

    @Value("${plc-os.mock:true}")
    private Boolean mockData;

    @Autowired
    private PlcRequest plcRequest;

    public static long defaultTimeOut = 1000;

    public List<PlcStatusRes> getPlcStatus() {
        PlcStatus status = plcRequest.getPlcStatus();
        if (status != null) {
            return status.getServiceStatus();
        }
        return Collections.emptyList();
    }

    public <T> T getPlcData(String uid, PlcReadDto<T> readDto, long intervalMillis) {
        PlcReadDto<Object> plcReadDto = getPlcData(Collections.singletonList((PlcReadDto<Object>)readDto), uid, intervalMillis).get(0);
        if (plcReadDto != null) {
            return Optional.ofNullable((PlcReadDto<T>) plcReadDto).map(PlcReadDto::getValue).orElse(null);
        }
        return null;
    }

    public List<PlcReadDto<Object>> getPlcData(List<PlcReadDto<Object>> readList, String uid, long intervalMillis) {
        if (ObjectUtils.isEmpty(readList)) {
            return Collections.emptyList();
        }
        if (mockData) {
            // 生成模拟数据
            Random random = new Random();
            readList.forEach(dto -> {
                String valueType = dto.getValueType();
                if ("int".equalsIgnoreCase(valueType)) {
                    dto.setActualValue(random.nextInt(1000));
                } else if ("float".equalsIgnoreCase(valueType)) {
                    dto.setActualValue(random.nextDouble() * 100);
                } else if ("double".equalsIgnoreCase(valueType)) {
                    dto.setActualValue(random.nextDouble() * 100);
                } else if ("bool".equalsIgnoreCase(valueType) || "boolean".equalsIgnoreCase(valueType)) {
                    dto.setActualValue(random.nextBoolean());
                } else {
                    // 默认生成字符串类型数据
                    dto.setActualValue(random.nextInt(100));
                }
            });
            return readList;
        }

        var request = buildReadReq(readList);
        PlcReadRes plcReadRes = plcRequest.getPlcData(request, uid);
        if (plcReadRes == null) {
            log.warn("plc service get failed");
            return Collections.emptyList();
        }
        var resList = Optional.ofNullable(plcReadRes.getData()).orElse(Collections.emptyList());

        Map<String, Object> allReads = resList.stream()
                .filter(CuBaseResponse.PlcSingleRes::success)
                .flatMap(e -> {
                    String dataAddr = e.getDataAddr();
                    Map<String, Object> simpleValues = e.getData().getSimpleValues();
                    return simpleValues.entrySet().stream().collect(Collectors.toMap(s -> PlcReadDto.concatAddress(dataAddr, s.getKey()), Map.Entry::getValue, (o, n) -> n)).entrySet().stream();
                }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (o, n) -> n));

        readList.forEach(e -> {
            Object value = allReads.get(e.getAddress());
            if (value != null) {
                e.setActualValue(value);
            }
        });
        return readList;
    }

    public List<PlcReadReq> buildReadReq(List<PlcReadDto<Object>> readList) {
        if (ObjectUtils.isEmpty(readList)) {
            return Collections.emptyList();
        }
        var plcReadMap = readList.stream().collect(Collectors.groupingBy(PlcReadDto::getAddressPrefix));
        return plcReadMap.entrySet().stream().map(e -> {
            if (ObjectUtils.isEmpty(e.getValue())) {
                return null;
            }
            PlcReadReq plcReadReq = new PlcReadReq();
            plcReadReq.setReqId(e.getKey());
            plcReadReq.setDataAddr(e.getKey());
            var points = e.getValue().stream()
                    .map(p -> PlcReadReq.PlcSimplePoint.builder()
                            .addr(p.getSubAddress())
                            .valueType(p.getValueType())
                            .optType("CachedRead")
                            .build()).toList();
            plcReadReq.setSimplePoints(points);
            return plcReadReq;
        }).filter(Objects::nonNull).toList();
    }

    public <T> boolean setPlcData(String uid, PlcWriteDto<T> writeDto) {
        return setPlcData(Collections.singletonList(writeDto), uid, defaultTimeOut);
    }

    public <T> boolean setPlcData(String uid, PlcWriteDto<T> writeDto, long intervalMillis) {
        return setPlcData(Collections.singletonList(writeDto), uid, intervalMillis);
    }

    public boolean setPlcData(List<PlcWriteDto<?>> writeList, String uid, long intervalMillis) {
        if (ObjectUtils.isEmpty(writeList)) {
            return true;
        }
        var request = buildWriteReq(writeList);
        log.info("plcWrite uid{}, req:{}", uid, request);
        PlcWriteRes plcWriteRes = null;
        if (mockData) {
            return true;
        } else {
            plcWriteRes = plcRequest.setPlcData(request, uid);
        }
        return plcWriteRes != null && plcWriteRes.success();
    }

    public List<PlcWriteReq> buildWriteReq(List<PlcWriteDto<?>> writeList) {
        if (ObjectUtils.isEmpty(writeList)) {
            return Collections.emptyList();
        }
        var plcReadMap = writeList.stream().collect(Collectors.groupingBy(PlcWriteDto::getAddressPrefix));
        return plcReadMap.entrySet().stream().map(e -> {
            if (ObjectUtils.isEmpty(e.getValue())) {
                return null;
            }
            PlcWriteReq plcWriteReq = new PlcWriteReq();
            plcWriteReq.setReqId(e.getKey());
            plcWriteReq.setDataAddr(e.getKey());
            var points = e.getValue().stream()
                    .map(p -> PlcWriteReq.PlcWriteSimplePoint.builder()
                            .addr(p.getSubAddress())
                            .targetValue(String.valueOf(p.getValue()))
                            .valueType(p.getValueType())
                            .optType("DirectWrite")
                            .build()).toList();
            plcWriteReq.setSimplePoints(points);
            return plcWriteReq;
        }).filter(Objects::nonNull).toList();
    }
}
