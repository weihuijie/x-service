//package com.x.data.collection.service.plc;
//
//import com.alibaba.fastjson2.JSON;
//import com.x.data.collection.service.plc.read.PlcReadDto;
//import com.x.data.collection.service.plc.read.PlcReadReq;
//import com.x.data.collection.service.plc.read.PlcReadRes;
//import com.x.data.collection.service.plc.write.PlcWriteDto;
//import com.x.data.collection.service.plc.write.PlcWriteReq;
//import com.x.data.collection.service.plc.write.PlcWriteRes;
//import jakarta.annotation.PostConstruct;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.ObjectUtils;
//import org.apache.curator.shaded.com.google.common.util.concurrent.Futures;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import java.time.Duration;
//import java.util.*;
//import java.util.stream.Collectors;
//
//import static org.apache.curator.shaded.com.google.common.util.concurrent.Futures.catching;
//
//@Slf4j
//@Service
//public class PlcService {
//
//    @Value("${plc-os.ip:127.0.0.1}")
//    private String cuServiceIp;
//    @Value("${plc-os.bizLog:false}")
//    private Boolean bizLog;
//    @Value("${plc-os.mock:true}")
//    private Boolean mockData;
//    private PlcApi plcApi;
//
//    public static long defaultTimeOut = 1000;
//
//    @PostConstruct
//    private void init() {
//        log.info("plcApi ip:{} ", cuServiceIp);
//        plcApi = HttpCall2.fromLocal("PlcApi", cuServiceIp, PlcApi.class).build();
//    }
//
//    public List<PlcStatusRes> getPlcStatus() {
//        PlcStatus status = catching(() -> Futures.getUnchecked(plcApi.getPlcStatus(), Duration.ofMillis(defaultTimeOut)));
//        if (status != null) {
//            return status.getServiceStatus();
//        }
//        return Collections.emptyList();
//    }
//
//    public <T> T getPlcData(String uid, PlcReadDto<T> readDto, long intervalMillis) {
//        PlcReadDto<Object> plcReadDto = getPlcData(Collections.singletonList((PlcReadDto<Object>)readDto), uid, intervalMillis).get(0);
//        if (plcReadDto != null) {
//            return Optional.ofNullable((PlcReadDto<T>) plcReadDto).map(PlcReadDto::getValue).orElse(null);
//        }
//        return null;
//    }
//
//    public List<PlcReadDto<Object>> getPlcData(List<PlcReadDto<Object>> readList, String uid, long intervalMillis) {
//        if (ObjectUtils.isEmpty(readList)) {
//            return Collections.emptyList();
//        }
//        if (mockData) {
//            return null;
//        }
//
//        var request = buildReadReq(readList);
//        if (bizLog) {
//            log.info("plcRead uid{}, req:{}", uid, JSON.toJSONString(request));
//        }
//        PlcReadRes plcReadRes = catching(() -> MoreFutures.getUnchecked(plcApi.getPlcData(request, uid), Duration.ofMillis(intervalMillis)), bizLog);
//        if (plcReadRes == null) {
//            log.warn("plc service get failed");
//            return Collections.emptyList();
//        }
//        if (bizLog) {
//            log.info("plcRead uid{}, res:{}", uid, JSON.toJSONString(plcReadRes));
//        }
//        var resList = Optional.ofNullable(plcReadRes.getData()).orElse(Collections.emptyList());
//
//        Map<String, Object> allReads = resList.stream()
//                .filter(CuBaseResponse.PlcSingleRes::success)
//                .flatMap(e -> {
//                    String dataAddr = e.getDataAddr();
//                    Map<String, Object> simpleValues = e.getData().getSimpleValues();
//                    return simpleValues.entrySet().stream().collect(Collectors.toMap(s -> PlcReadDto.concatAddress(dataAddr, s.getKey()), Map.Entry::getValue, (o, n) -> n)).entrySet().stream();
//                }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (o, n) -> n));
//
//        readList.forEach(e -> {
//            Object value = allReads.get(e.getAddress());
//            if (value != null) {
//                e.setActualValue(value);
//            }
//        });
//        return readList;
//    }
//
//    public List<PlcReadReq> buildReadReq(List<PlcReadDto<Object>> readList) {
//        if (ObjectUtils.isEmpty(readList)) {
//            return Collections.emptyList();
//        }
//        var plcReadMap = readList.stream().collect(Collectors.groupingBy(PlcReadDto::getAddressPrefix));
//        return plcReadMap.entrySet().stream().map(e -> {
//            if (ObjectUtils.isEmpty(e.getValue())) {
//                return null;
//            }
//            PlcReadReq plcReadReq = new PlcReadReq();
//            plcReadReq.setReqId(e.getKey());
//            plcReadReq.setDataAddr(e.getKey());
//            var points = e.getValue().stream()
//                    .map(p -> PlcReadReq.PlcSimplePoint.builder()
//                            .addr(p.getSubAddress())
//                            .valueType(p.getValueType())
//                            .optType("CachedRead")
//                            .build()).toList();
//            plcReadReq.setSimplePoints(points);
//            return plcReadReq;
//        }).filter(Objects::nonNull).toList();
//    }
//
//    public <T> boolean setPlcData(String uid, PlcWriteDto<T> writeDto) {
//        return setPlcData(Collections.singletonList(writeDto), uid, defaultTimeOut);
//    }
//
//    public <T> boolean setPlcData(String uid, PlcWriteDto<T> writeDto, long intervalMillis) {
//        return setPlcData(Collections.singletonList(writeDto), uid, intervalMillis);
//    }
//
//    public boolean setPlcData(List<PlcWriteDto<?>> writeList, String uid, long intervalMillis) {
//        if (ObjectUtils.isEmpty(writeList)) {
//            return true;
//        }
//        var request = buildWriteReq(writeList);
//        log.info("plcWrite uid{}, req:{}", uid, request);
//        PlcWriteRes plcReadRes = null;
//        if (mockData) {
//            return true;
//        } else {
//            plcReadRes = catching(() -> MoreFutures.getUnchecked(plcApi.setPlcData(request, uid), Duration.ofMillis(intervalMillis)));
//        }
//        log.info("plcWrite uid{}, res:{}", uid, plcReadRes);
//        return plcReadRes != null && plcReadRes.success();
//    }
//
//    public List<PlcWriteReq> buildWriteReq(List<PlcWriteDto<?>> writeList) {
//        if (ObjectUtils.isEmpty(writeList)) {
//            return Collections.emptyList();
//        }
//        var plcReadMap = writeList.stream().collect(Collectors.groupingBy(PlcWriteDto::getAddressPrefix));
//        return plcReadMap.entrySet().stream().map(e -> {
//            if (ObjectUtils.isEmpty(e.getValue())) {
//                return null;
//            }
//            PlcWriteReq plcWriteReq = new PlcWriteReq();
//            plcWriteReq.setReqId(e.getKey());
//            plcWriteReq.setDataAddr(e.getKey());
//            var points = e.getValue().stream()
//                    .map(p -> PlcWriteReq.PlcWriteSimplePoint.builder()
//                            .addr(p.getSubAddress())
//                            .targetValue(String.valueOf(p.getValue()))
//                            .valueType(p.getValueType())
//                            .optType("DirectWrite")
//                            .build()).toList();
//            plcWriteReq.setSimplePoints(points);
//            return plcWriteReq;
//        }).filter(Objects::nonNull).toList();
//    }
//}
