//package com.x.data.collection.service.channel.iotdb.service.impl;
//
//import com.alibaba.fastjson2.JSONArray;
//import com.alibaba.fastjson2.JSONObject;
//import com.x.data.collection.service.channel.iotdb.config.IotDbSessionPoolManager;
//import com.x.data.collection.service.channel.iotdb.dto.*;
//import com.x.data.collection.service.channel.iotdb.service.IotDBService;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.ObjectUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.iotdb.isession.SessionDataSet;
//import org.apache.iotdb.isession.pool.SessionDataSetWrapper;
//import org.apache.iotdb.session.pool.SessionPool;
//import org.apache.iotdb.tsfile.file.metadata.enums.TSDataType;
//import org.apache.iotdb.tsfile.read.common.Field;
//import org.apache.iotdb.tsfile.read.common.RowRecord;
//import org.apache.iotdb.tsfile.write.record.Tablet;
//import org.apache.iotdb.tsfile.write.schema.MeasurementSchema;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.*;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//@Slf4j
//@Service
//public class IotDBServiceImpl implements IotDBService {
//
//    @Autowired
//    IotDbSessionPoolManager ioTDBSessionPoolManager;
//
//    @Override
//    public List<RealtimeValueDto> realtime(String db, String device, List<String> points) {
//        //如果请求为空 直接返回null
//        if (ObjectUtils.isEmpty(db) || StringUtils.isEmpty(device) || ObjectUtils.isEmpty(points)) {
//            return Collections.emptyList();
//        }
//
//        SessionPool session = ioTDBSessionPoolManager.getSessionPool();
//        try (SessionDataSetWrapper dataSetWrapper = session.executeLastDataQueryForOneDevice(db, device, points, false)) {
//            SessionDataSet resultSet = dataSetWrapper.getSessionDataSet();
//            List<RealtimeValueDto> realtimeValueDtoList = new ArrayList<>();
//            while (resultSet.hasNext()) {
//                RealtimeValueDto realtimeValueDto = new RealtimeValueDto();
//                RowRecord record = resultSet.next();
//                realtimeValueDto.setTs(record.getTimestamp());
//                List<Field> fields = record.getFields();
//                realtimeValueDto.setTimeSeries(fields.get(0).getStringValue());
//                realtimeValueDto.setVal(fields.get(1).getStringValue());
//                realtimeValueDtoList.add(realtimeValueDto);
//            }
//            return realtimeValueDtoList;
//        } catch (Exception ex) {
//            log.error("realtime error, db: {}, device: {}, points: {}", db, device, points, ex);
//            throw new RuntimeException(ex);
//        }
//    }
//
//    @Override
//    public List<AvgValueDto> avgValue(String device, List<String> points, Long startTime, Long endTime) {
//        //如果请求为空 直接返回null
//        if (StringUtils.isEmpty(device) || ObjectUtils.isEmpty(points)) {
//            return Collections.emptyList();
//        }
//
//        // 构建查询语句
//        String sql = buildAvgValueQuery(device, points, startTime, endTime);
//        log.info("avgValue sql::{}", sql);
//
//        SessionPool session = ioTDBSessionPoolManager.getSessionPool();
//        // 执行查询
//        try (SessionDataSetWrapper dataSetWrapper = session.executeQueryStatement(sql)) {
//            SessionDataSet resultSet = dataSetWrapper.getSessionDataSet();
//
//            List<AvgValueDto> avgValueList = new ArrayList<>();
//            while (resultSet.hasNext()) {
//                RowRecord record = resultSet.next();
//                List<Field> fields = record.getFields();
//                for (int i = 0; i < fields.size(); i++) {
//                    AvgValueDto avgValue = AvgValueDto.builder().build();
//                    avgValue.setPoint(points.get(i));
//                    if (!Objects.equals(fields.get(i).getStringValue(), "null")) {
//                        avgValue.setVal(fields.get(i).getDoubleV());
//                    }
//                    avgValueList.add(avgValue);
//                }
//            }
//            return avgValueList;
//        } catch (Exception ex) {
//            log.error("avgValue error, device: {}, points: {}, startTime: {}, endTime: {}",
//                    device, points, startTime, endTime, ex);
//            throw new RuntimeException(ex);
//        }
//    }
//
//    @Override
//    public List<StatisticsValueDto> statisticsValue(String device, List<String> points, Long startTime, Long endTime) {
//        //如果请求为空 直接返回null
//        if (StringUtils.isEmpty(device) || ObjectUtils.isEmpty(points)) {
//            return Collections.emptyList();
//        }
//
//        // 构建查询语句
//        String sql = buildStatisticsValueQuery(device, points, startTime, endTime);
//        log.info("statisticsValue sql :: {}", sql);
//
//        // 执行查询
//        SessionPool session = ioTDBSessionPoolManager.getSessionPool();
//        try (SessionDataSetWrapper dataSetWrapper = session.executeQueryStatement(sql)) {
//            SessionDataSet resultSet = dataSetWrapper.getSessionDataSet();
//
//            List<StatisticsValueDto> statisticsValueDtoList = new ArrayList<>();
//            while (resultSet.hasNext()) {
//                RowRecord record = resultSet.next();
//                List<Field> fields = record.getFields();
//                for (int i = 0; i < points.size(); i++) {
//                    StatisticsValueDto value = StatisticsValueDto.builder().build();
//                    value.setPoint(points.get(i));
//                    value.setMaxVal(fields.get(i * 2).getStringValue());
//                    value.setMinVal(fields.get(i * 2 + 1).getStringValue());
//                    statisticsValueDtoList.add(value);
//                }
//            }
//            return statisticsValueDtoList;
//        } catch (Exception ex) {
//            log.error("statisticsValue error, device: {}, points: {}, startTime: {}, endTime: {}",
//                    device, points, startTime, endTime, ex);
//            throw new RuntimeException(ex);
//        }
//    }
//
//    @Override
//    public List<SumValueDto> sumValue(String device, List<String> points, Long startTime, Long endTime) {
//        //如果请求为空 直接返回null
//        if (StringUtils.isEmpty(device) || ObjectUtils.isEmpty(points)) {
//            return Collections.emptyList();
//        }
//
//        // 构建查询语句
//        String sql = buildSumValueQuery(device, points, startTime, endTime);
//        log.info("sumValue sql::{}", sql);
//
//        // 执行查询
//        SessionPool session = ioTDBSessionPoolManager.getSessionPool();
//        try (SessionDataSetWrapper dataSetWrapper = session.executeQueryStatement(sql)) {
//            SessionDataSet resultSet = dataSetWrapper.getSessionDataSet();
//
//            List<SumValueDto> sumValueDtoList = new ArrayList<>();
//            while (resultSet.hasNext()) {
//                RowRecord record = resultSet.next();
//                List<Field> fields = record.getFields();
//                for (int i = 0; i < fields.size(); i++) {
//                    SumValueDto value = SumValueDto.builder().build();
//                    value.setPoint(points.get(i));
//                    if (!Objects.equals(fields.get(i).getStringValue(), "null")) {
//                        value.setVal(fields.get(i).getDoubleV());
//                    }
//                    sumValueDtoList.add(value);
//                }
//            }
//            return sumValueDtoList;
//        } catch (Exception ex) {
//            log.error("sumValue error, device: {}, points: {}, startTime: {}, endTime: {}",
//                    device, points, startTime, endTime, ex);
//            throw new RuntimeException(ex);
//        }
//    }
//
//    @Override
//    public HistoryValuesDto historyData(String device, List<String> points, Integer page, Integer pageSize, Long startTime, Long endTime) {
//        // 如果请求为空，直接返回null
//        if (StringUtils.isEmpty(device) || ObjectUtils.isEmpty(points) || page == null || pageSize == null) {
//            return null;
//        }
//
//        // 执行计数查询
//        long totalRecords = getQueryResultCount(device, points, startTime, endTime);
//        if (totalRecords == 0) {
//            // 如果没有记录，直接返回null
//            return null;
//        }
//
//        // 将页码转换为基于0的索引
//        int offset = (page - 1) * pageSize;
//
//        // 构建查询语句
//        String sql = buildHistoryDataQuery(device, points, startTime, endTime, offset, pageSize);
//        log.info("historyData sql::{}", sql);
//
//        // 执行查询
//        SessionPool session = ioTDBSessionPoolManager.getSessionPool();
//        try (SessionDataSetWrapper dataSetWrapper = session.executeQueryStatement(sql)) {
//            SessionDataSet resultSet = dataSetWrapper.getSessionDataSet();
//
//            HistoryValuesDto historyValuesDto = new HistoryValuesDto();
//            Map<String, HistoryDataDto> historyDataMap = new HashMap<>();
//
//            // 计算总页数
//            int totalPage = (int) Math.ceil((double) totalRecords / pageSize);
//
//            while (resultSet.hasNext()) {
//                RowRecord record = resultSet.next();
//                // 获取时间戳
//                long timestamp = record.getTimestamp();
//                List<Field> fields = record.getFields();
//
//                // 遍历所有数据点
//                for (int i = 0; i < points.size(); i++) {
//                    String point = points.get(i); // 当前数据点的名称
//                    Object val = fields.get(i).getStringValue(); // 当前数据点的值
//
//                    // 创建或获取HistoryData对象
//                    HistoryDataDto historyData = historyDataMap.computeIfAbsent(point, k -> {
//                        HistoryDataDto newHistoryData = new HistoryDataDto();
//                        newHistoryData.setPoint(k);
//                        newHistoryData.setValues(new ArrayList<>());
//                        return newHistoryData;
//                    });
//
//                    // 创建HistoryValue对象并添加到HistoryData的values列表中
//                    HistoryValueDto historyValueDto = new HistoryValueDto();
//                    historyValueDto.setTimestamp(timestamp);
//                    historyValueDto.setVal(val);
//                    historyData.getValues().add(historyValueDto);
//                }
//            }
//
//            // 将Map中的所有HistoryData对象添加到列表中
//            List<HistoryDataDto> historyDataList = new ArrayList<>(historyDataMap.values());
//            historyValuesDto.setHistoryData(historyDataList);
//            historyValuesDto.setTotal((int) totalRecords);
//            historyValuesDto.setPageSize(pageSize);
//            historyValuesDto.setPage(totalPage);
//            historyValuesDto.setCurrent(page);
//            return historyValuesDto;
//        } catch (Exception ex) {
//            log.error("historyData error, device: {}, points: {}, page: {}, pageSize: {}, startTime: {}, endTime: {}",
//                    device, points, page, pageSize, startTime, endTime, ex);
//            throw new RuntimeException(ex);
//        }
//    }
//
//    @Override
//    public HistoryValuesDto windowAggHistoryData(String device, List<String> points, Integer page, Integer pageSize, Long startTime, Long endTime, Integer windowSize, String interval) {
//        // 如果请求为空，直接返回null
//        if (StringUtils.isEmpty(device) || ObjectUtils.isEmpty(points) || page == null || pageSize == null || windowSize == null || StringUtils.isEmpty(interval)) {
//            return null;
//        }
//
//        // 执行计数查询
//        long totalRecords = getQueryResultCountForSingleDevices(device, points, startTime, endTime, windowSize, interval);
//        if (totalRecords == 0) {
//            // 如果没有记录，直接返回null
//            return null;
//        }
//
//        // 将页码转换为基于0的索引
//        int offset = (page - 1) * pageSize;
//
//        // 构建查询语句
//        String sql = buildWindowAggQuery(device, points, startTime, endTime, windowSize, interval, offset, pageSize);
//        log.info("windowAggHistoryData sql::{}", sql);
//
//        // 执行查询
//        SessionPool session = ioTDBSessionPoolManager.getSessionPool();
//        try (SessionDataSetWrapper dataSetWrapper = session.executeQueryStatement(sql)) {
//            SessionDataSet resultSet = dataSetWrapper.getSessionDataSet();
//
//            HistoryValuesDto historyValuesDto = new HistoryValuesDto();
//            List<HistoryDataDto> historyDataList = new ArrayList<>();
//
//            // 计算总页数
//            int totalPage = (int) Math.ceil((double) totalRecords / pageSize);
//
//            log.info("totalPage::{},totalRecords::{},::pageSize{}", totalPage, totalRecords, pageSize);
//
//            while (resultSet.hasNext()) {
//                RowRecord record = resultSet.next();
//                // 获取时间戳
//                long timestamp = record.getTimestamp();
//                List<Field> fields = record.getFields();
//
//                // 遍历所有数据点
//                for (int i = 0; i < points.size(); i++) {
//                    String point = points.get(i); // 当前数据点的名称
//                    Object val = fields.get(i).getStringValue(); // 当前数据点的值
//
//                    // 创建或获取HistoryData对象
//                    HistoryDataDto historyData = historyDataList.stream()
//                            .filter(h -> h.getPoint().equals(point))
//                            .findFirst()
//                            .orElseGet(() -> {
//                                HistoryDataDto newHistoryData = new HistoryDataDto();
//                                newHistoryData.setPoint(point);
//                                newHistoryData.setValues(new ArrayList<>());
//                                historyDataList.add(newHistoryData);
//                                return newHistoryData;
//                            });
//
//                    // 创建HistoryValue对象并添加到HistoryData的values列表中
//                    HistoryValueDto historyValueDto = new HistoryValueDto();
//                    historyValueDto.setTimestamp(timestamp);
//                    historyValueDto.setVal(val);
//                    historyData.getValues().add(historyValueDto);
//                }
//            }
//
//            historyValuesDto.setHistoryData(historyDataList);
//            historyValuesDto.setTotal((int) totalRecords);
//            historyValuesDto.setPageSize(pageSize);
//            historyValuesDto.setPage(totalPage);
//            historyValuesDto.setCurrent(page);
//            return historyValuesDto;
//        } catch (Exception ex) {
//            log.error("windowAggHistoryData error, device: {}, points: {}, page: {}, pageSize: {}, startTime: {}, endTime: {}, windowSize: {}, interval: {}",
//                    device, points, page, pageSize, startTime, endTime, windowSize, interval, ex);
//            throw new RuntimeException(ex);
//        }
//    }
//
//    /**
//     * 插入数据到IoTDB
//     *
//     * @param deviceId 设备的唯一标识符
//     * @param pointArr 要插入的数据点名称列表
//     * @param types    各数据点对应的数据类型列表
//     * @param dataJson 包含要插入的数据的JSON字符串
//     *                 timestamp 数据点的时间戳，单位为毫秒。如果不传，则默认为当前时间。
//     */
//    @Override
//    public int insertData(String deviceId, List<String> pointArr, List<TSDataType> types, String dataJson) {
//        List<MeasurementSchema> schemaList = new ArrayList<>();
//        int insertedRowCount = 0; // 用于跟踪成功插入的行数
//        try {
//            SessionPool session = ioTDBSessionPoolManager.getSessionPool();
//            for (int i = 0; i < pointArr.size(); i++) {
//                schemaList.add(new MeasurementSchema(pointArr.get(i), types.get(i)));
//            }
//            JSONObject dataJsonObj = JSONObject.parseObject(dataJson);
//            JSONArray dataArray = dataJsonObj.getJSONArray("data");
//
//            if (dataArray != null && !dataArray.isEmpty()) {
//                Tablet tablet = new Tablet(deviceId, schemaList, dataArray.size());
//                tablet.rowSize = dataArray.size();
//                for (int i = 0; i < tablet.rowSize; i++) {
//                    JSONObject dataObj = dataArray.getJSONObject(i);
//                    long timestamp = dataObj.getLong("timestamp");
//                    tablet.addTimestamp(i, timestamp);
//                    for (int j = 0; j < pointArr.size(); j++) {
//                        String point = pointArr.get(j);
//                        if (dataObj.containsKey(point)) { // 修复containsValue为containsKey
//                            Object value = dataObj.get(point);
//                            // 根据数据类型将值添加到 Tablet
//                            TSDataType type = types.get(j);
//                            switch (type) {
//                                case BOOLEAN:
//                                    tablet.addValue(point, i, Boolean.parseBoolean(value.toString()));
//                                    break;
//                                case INT32:
//                                    tablet.addValue(point, i, Integer.parseInt(value.toString()));
//                                    break;
//                                case INT64:
//                                    tablet.addValue(point, i, Long.parseLong(value.toString()));
//                                    break;
//                                case FLOAT:
//                                    tablet.addValue(point, i, Float.parseFloat(value.toString()));
//                                    break;
//                                case DOUBLE:
//                                    tablet.addValue(point, i, Double.parseDouble(value.toString()));
//                                    break;
//                                case TEXT:
//                                    tablet.addValue(point, i, value.toString());
//                                    break;
//                                default:
//                                    log.warn("Unsupported data type: {}", type);
//                            }
//                        }
//                    }
//                }
//                if (tablet.rowSize != 0) {
//                    session.insertTablet(tablet);
//                    log.info("Inserted data into device: {}", deviceId);
//                    insertedRowCount = tablet.rowSize; // 更新成功插入的行数
//                }
//            }
//        } catch (Exception ex) {
//            log.error("insertData error, deviceId: {}, pointArr: {}, dataJson: {}", deviceId, pointArr, dataJson, ex);
//            throw new RuntimeException(ex);
//        }
//        return insertedRowCount; // 返回成功插入的行数
//    }
//
//    @Override
//    public List<BatchRealtimeValueDto> batchRealtime(List<String> deviceIds, List<String> points) {
//        if (ObjectUtils.isEmpty(deviceIds) || ObjectUtils.isEmpty(points)) {
//            return Collections.emptyList();
//        }
//
//        try {
//            String sql = buildLastDataQuery(deviceIds, points);
//            log.info("batchRealtime sql::{}", sql);
//            SessionPool session = ioTDBSessionPoolManager.getSessionPool();
//            try (SessionDataSetWrapper dataSetWrapper = session.executeQueryStatement(sql)) {
//                SessionDataSet resultSet = dataSetWrapper.getSessionDataSet();
//
//                // 用于存储每个设备的最新数据
//                Map<String, BatchRealtimeValueDto> deviceDataMap = new HashMap<>();
//
//                while (resultSet.hasNext()) {
//                    // 获取设备路径和时间序列名称和时间戳
//                    RowRecord record = resultSet.next();
//                    long timestamp = record.getTimestamp();
//                    String timeSeries = record.getFields().get(0).getStringValue();
//                    String currentDeviceId = timeSeries.substring(0, timeSeries.lastIndexOf("."));
//
//                    // 初始化设备的最新数据对象
//                    BatchRealtimeValueDto batchRealtimeValue = deviceDataMap.computeIfAbsent(currentDeviceId, k -> {
//                        BatchRealtimeValueDto newValue = new BatchRealtimeValueDto();
//                        newValue.setDeviceId(k);
//                        newValue.setRealtimeData(new ArrayList<>());
//                        return newValue;
//                    });
//
//                    RealtimeValueDto realtimeValueDto = new RealtimeValueDto();
//                    realtimeValueDto.setTs(timestamp);
//                    realtimeValueDto.setTimeSeries(timeSeries);
//                    // 假设数据类型为 STRING，需要根据实际数据类型进行转换
//                    realtimeValueDto.setVal(record.getFields().get(1).getStringValue());
//                    batchRealtimeValue.getRealtimeData().add(realtimeValueDto);
//                }
//
//                // 将所有设备的最新数据添加到结果列表
//                return new ArrayList<>(deviceDataMap.values());
//            }
//        } catch (Exception ex) {
//            log.error("batchRealtime error, deviceIds: {}, points: {}", deviceIds, points, ex);
//            throw new RuntimeException(ex);
//        }
//    }
//
//    @Override
//    public BatchHistoryValuesDto batchHistoryData(List<String> deviceIds, List<String> points, Integer page, Integer pageSize, Long startTime, Long endTime) {
//        // 如果请求为空，直接返回null
//        if (ObjectUtils.isEmpty(deviceIds) || ObjectUtils.isEmpty(points) || page == null || pageSize == null) {
//            return null;
//        }
//
//        // 执行计数查询
//        long totalRecords = getQueryResultCountForMultipleDevices(deviceIds, points, startTime, endTime);
//        if (totalRecords == 0) {
//            // 如果没有记录，直接返回null
//            return null;
//        }
//
//        // 将页码转换为基于0的索引
//        int offset = (page - 1) * pageSize;
//        // 计算总页数和偏移量
//        int totalPage = (int) Math.ceil((double) totalRecords / pageSize);
//
//        // 构建查询语句
//        String sql = buildHistoryDataQuery(deviceIds, points, startTime, endTime, offset, pageSize);
//        log.info("batchHistoryData sql::{}", sql);
//
//        SessionPool session = ioTDBSessionPoolManager.getSessionPool();
//        try (SessionDataSetWrapper dataSetWrapper = session.executeQueryStatement(sql)) {
//            SessionDataSet resultSet = dataSetWrapper.getSessionDataSet();
//
//            BatchHistoryValuesDto batchHistoryValuesDto = new BatchHistoryValuesDto();
//            Map<String, HistoryDataByDeviceDto> deviceDataMap = new HashMap<>();
//
//            // 获取列名
//            List<String> columnNames = resultSet.getColumnNames();
//
//            while (resultSet.hasNext()) {
//                RowRecord record = resultSet.next();
//                long timestamp = record.getTimestamp();
//
//                // 遍历字段，从第一个字段开始（跳过时间字段）
//                for (int i = 1; i < columnNames.size(); i++) { // 从1开始跳过时间字段
//                    String fieldName = columnNames.get(i);
//                    Object value = record.getFields().get(i - 1).getStringValue(); // 获取值
//                    // 根据字段名解析设备 ID 和测点名
//                    String deviceId = fieldName.substring(0, fieldName.lastIndexOf(".")); // 设备 ID
//                    String point = fieldName.substring(fieldName.lastIndexOf(".") + 1); // 测点名
//
//                    // 获取或创建 HistoryDataByDeviceDto 对象
//                    HistoryDataByDeviceDto deviceData = deviceDataMap.computeIfAbsent(deviceId, k -> {
//                        HistoryDataByDeviceDto newDeviceData = new HistoryDataByDeviceDto();
//                        newDeviceData.setDeviceId(k);
//                        newDeviceData.setHistoryData(new ArrayList<>());
//                        return newDeviceData;
//                    });
//
//                    // 创建或获取 HistoryDataDto 对象
//                    HistoryDataDto historyData = deviceData.getHistoryData().stream()
//                            .filter(h -> h.getPoint().equals(point))
//                            .findFirst()
//                            .orElseGet(() -> {
//                                HistoryDataDto newHistoryData = new HistoryDataDto();
//                                newHistoryData.setPoint(point);
//                                newHistoryData.setValues(new ArrayList<>());
//                                deviceData.getHistoryData().add(newHistoryData);
//                                return newHistoryData;
//                            });
//
//                    // 创建并添加 HistoryValueDto 对象
//                    HistoryValueDto historyValueDto = new HistoryValueDto();
//                    historyValueDto.setTimestamp(timestamp);
//                    historyValueDto.setVal(value);
//                    historyData.getValues().add(historyValueDto);
//                }
//            }
//
//            List<HistoryDataByDeviceDto> historyDataByDeviceList = new ArrayList<>(deviceDataMap.values());
//            batchHistoryValuesDto.setHistoryDataByDevice(historyDataByDeviceList);
//            batchHistoryValuesDto.setTotal((int) totalRecords);
//            batchHistoryValuesDto.setPage(totalPage);
//            batchHistoryValuesDto.setCurrent(page);
//            batchHistoryValuesDto.setPageSize(pageSize);
//
//            return batchHistoryValuesDto;
//        } catch (Exception ex) {
//            log.error("batchHistoryData error, deviceIds: {}, points: {}, page: {}, pageSize: {}, startTime: {}, endTime: {}",
//                    deviceIds, points, page, pageSize, startTime, endTime, ex);
//            throw new RuntimeException("Error fetching batch history data", ex);
//        }
//    }
//
//    @Override
//    public BatchHistoryValuesDto batchWindowAggHistoryData(List<String> deviceIds, List<String> points, Integer page, Integer pageSize, Long startTime, Long endTime, Integer windowSize, String interval) {
//        // 如果请求为空，直接返回null
//        if (ObjectUtils.isEmpty(deviceIds) || ObjectUtils.isEmpty(points) || page == null || pageSize == null || windowSize == null || interval == null) {
//            return null;
//        }
//
//        // 执行计数查询
//        long totalRecords = getQueryResultCountForMultipleDevices(deviceIds, points, startTime, endTime, windowSize, interval, pageSize, page);
//        if (totalRecords == 0) {
//            return null;
//        }
//
//        // 分页计算
//        int offset = (page - 1) * pageSize;
//        int totalPage = (int) Math.ceil((double) totalRecords / pageSize);
//
//        // 构建查询语句
//        String sql = buildBatchWindowAggQuery(deviceIds, points, startTime, endTime, windowSize, interval, offset, pageSize);
//        log.info("batchWindowAggHistoryData sql::{}", sql);
//
//        SessionPool session = ioTDBSessionPoolManager.getSessionPool();
//        try (SessionDataSetWrapper dataSetWrapper = session.executeQueryStatement(sql)) {
//            SessionDataSet resultSet = dataSetWrapper.getSessionDataSet();
//
//            BatchHistoryValuesDto batchHistoryValuesDto = new BatchHistoryValuesDto();
//            Map<String, HistoryDataByDeviceDto> deviceDataMap = new HashMap<>();
//
//            // 获取列名
//            List<String> columnNames = resultSet.getColumnNames();
//
//            while (resultSet.hasNext()) {
//                RowRecord record = resultSet.next();
//                long timestamp = record.getTimestamp();
//
//                // 遍历字段，从第一个字段开始（跳过时间字段）
//                for (int i = 1; i < columnNames.size(); i++) { // 从1开始跳过时间字段
//                    String fieldName = columnNames.get(i);
//                    Object value = record.getFields().get(i - 1).getStringValue(); // 获取值
//
//                    // fieldName 形式如 "avg(root.ln.wf01.wt01.temperature)"
//                    // 确保提取正确的设备 ID 和测点名
//                    String cleanedFieldName = fieldName.trim(); // 去掉首尾空格
//
//                    // 使用正则表达式来提取设备 ID 和测点名
//                    // 修改这里的正则表达式以提取完整的 deviceId
//                    Pattern pattern = Pattern.compile("avg\\((.+?)\\.([\\w`]+)\\)"); // 匹配 deviceId 和 point
//                    Matcher matcher = pattern.matcher(cleanedFieldName);
//
//                    if (matcher.find()) {
//                        String deviceId = matcher.group(1); // 获取到除最后一段的设备 ID
//                        String point = matcher.group(2); // 获取最后一段作为测点名
//                        // 创建或获取 HistoryDataByDeviceDto 对象
//                        HistoryDataByDeviceDto deviceData = deviceDataMap.computeIfAbsent(deviceId, k -> {
//                            HistoryDataByDeviceDto newDeviceData = new HistoryDataByDeviceDto();
//                            newDeviceData.setDeviceId(k);
//                            newDeviceData.setHistoryData(new ArrayList<>());
//                            return newDeviceData;
//                        });
//
//                        // 创建或获取 HistoryDataDto 对象
//                        HistoryDataDto historyData = deviceData.getHistoryData().stream()
//                                .filter(h -> h.getPoint().equals(point))
//                                .findFirst()
//                                .orElseGet(() -> {
//                                    HistoryDataDto newHistoryData = new HistoryDataDto();
//                                    newHistoryData.setPoint(point);
//                                    newHistoryData.setValues(new ArrayList<>());
//                                    deviceData.getHistoryData().add(newHistoryData);
//                                    return newHistoryData;
//                                });
//
//                        // 创建并添加 HistoryValueDto 对象
//                        HistoryValueDto historyValueDto = new HistoryValueDto();
//                        historyValueDto.setTimestamp(timestamp);
//                        historyValueDto.setVal(value);
//                        historyData.getValues().add(historyValueDto);
//                    }
//                }
//            }
//
//            List<HistoryDataByDeviceDto> historyDataByDeviceList = new ArrayList<>(deviceDataMap.values());
//            batchHistoryValuesDto.setHistoryDataByDevice(historyDataByDeviceList);
//            batchHistoryValuesDto.setTotal((int) totalRecords);
//            batchHistoryValuesDto.setPage(totalPage);
//            batchHistoryValuesDto.setCurrent(page);
//            batchHistoryValuesDto.setPageSize(pageSize);
//
//            return batchHistoryValuesDto;
//        } catch (Exception ex) {
//            log.error("batchWindowAggHistoryData error, deviceIds: {}, points: {}, page: {}, pageSize: {}, startTime: {}, endTime: {}, windowSize: {}, interval: {}",
//                    deviceIds, points, page, pageSize, startTime, endTime, windowSize, interval, ex);
//            throw new RuntimeException("Error fetching batch history data", ex);
//        }
//    }
//
//    @Override
//    public List<DeviceConditionDto> conditionDevice(List<String> deviceIds, List<String> points, Long startTime, Long endTime, String sql) {
//        // 如果请求为空，直接返回null
//        if (ObjectUtils.isEmpty(deviceIds) || ObjectUtils.isEmpty(points)) {
//            return null;
//        }
//        // 构建查询语句
//        if (StringUtils.isEmpty(sql)){
//            // 构建设备完整路径
//            List<String> devicePaths = new ArrayList<>();
//            for (String deviceId : deviceIds) {
//                devicePaths.add("root.haulotte.trans.**."+deviceId+".*");
//            }
//            sql = buildHistoryDataQuery(devicePaths, points, startTime, endTime, null, null);
//        }
//        log.info("conditionDevice sql::{}", sql);
//
//        SessionPool session = ioTDBSessionPoolManager.getSessionPool();
//        try (SessionDataSetWrapper dataSetWrapper = session.executeQueryStatement(sql)) {
//            SessionDataSet resultSet = dataSetWrapper.getSessionDataSet();
//            log.info("conditionDevice resultSet::{}", resultSet);
//            BatchHistoryValuesDto batchHistoryValuesDto = new BatchHistoryValuesDto();
//            Map<String, HistoryDataByDeviceDto> deviceDataMap = new HashMap<>();
//
//            // 获取列名
//            List<String> columnNames = resultSet.getColumnNames();
//
//            while (resultSet.hasNext()) {
//                RowRecord record = resultSet.next();
//                long timestamp = record.getTimestamp();
//
//                // 遍历字段，从第一个字段开始（跳过时间字段）
//                for (int i = 1; i < columnNames.size(); i++) { // 从1开始跳过时间字段
//                    String fieldName = columnNames.get(i);
//                    Object value = record.getFields().get(i - 1).getStringValue(); // 获取值
//                    // 根据字段名解析设备 ID 和测点名
//                    String deviceId = fieldName.substring(0, fieldName.lastIndexOf(".")); // 设备 ID
//                    String point = fieldName.substring(fieldName.lastIndexOf(".") + 1); // 测点名
//
//                    // 获取或创建 HistoryDataByDeviceDto 对象
//                    HistoryDataByDeviceDto deviceData = deviceDataMap.computeIfAbsent(deviceId, k -> {
//                        HistoryDataByDeviceDto newDeviceData = new HistoryDataByDeviceDto();
//                        newDeviceData.setDeviceId(k);
//                        newDeviceData.setHistoryData(new ArrayList<>());
//                        return newDeviceData;
//                    });
//
//                    // 创建或获取 HistoryDataDto 对象
//                    HistoryDataDto historyData = deviceData.getHistoryData().stream()
//                            .filter(h -> h.getPoint().equals(point))
//                            .findFirst()
//                            .orElseGet(() -> {
//                                HistoryDataDto newHistoryData = new HistoryDataDto();
//                                newHistoryData.setPoint(point);
//                                newHistoryData.setValues(new ArrayList<>());
//                                deviceData.getHistoryData().add(newHistoryData);
//                                return newHistoryData;
//                            });
//
//                    // 创建并添加 HistoryValueDto 对象
//                    HistoryValueDto historyValueDto = new HistoryValueDto();
//                    historyValueDto.setTimestamp(timestamp);
//                    historyValueDto.setVal(value);
//                    historyData.getValues().add(historyValueDto);
//                }
//            }
//
//            List<DeviceConditionDto> historyDataByDeviceList = new ArrayList<>();
//
//            return null;
//        } catch (Exception ex) {
//            log.error("batchHistoryData error, deviceIds: {}, points: {}, page: {}, pageSize: {}, startTime: {}, endTime: {}",
//                    deviceIds, points, "","" , startTime, endTime, ex);
//            throw new RuntimeException("Error fetching batch history data", ex);
//        }
//    }
//
//    public long getQueryResultCountForSingleDevices(String device, List<String> points, Long startTime, Long endTime, Integer windowSize, String interval) {
//        SessionPool session = ioTDBSessionPoolManager.getSessionPool();
//        try {
//            // 构建查询语句
//            String sql = buildWindowAggQuery(device, points, startTime, endTime, windowSize, interval);
//            log.info("getQueryResultCountForSingleDevices sql::{}", sql);
//
//            // 执行查询
//            try (SessionDataSetWrapper dataSetWrapper = session.executeQueryStatement(sql)) {
//                SessionDataSet resultSet = dataSetWrapper.getSessionDataSet();
//
//                // 由于是聚合查询，获取行数的方法是简单地调用 next()，不需要记录具体的字段
//                long count = 0;
//                while (resultSet.hasNext()) {
//                    resultSet.next();
//                    count++;
//                }
//                return count;
//            }
//        } catch (Exception ex) {
//            log.error("getQueryResultCountForSingleDevices error, device: {}, points: {}, startTime: {}, endTime: {}, windowSize: {}, interval: {}",
//                    device, points, startTime, endTime, windowSize, interval, ex);
//            throw new RuntimeException(ex);
//        }
//    }
//
//    public long getQueryResultCountForMultipleDevices(List<String> devices, List<String> points, Long startTime, Long endTime, Integer windowSize, String interval, int offset, int limit) {
//        SessionPool session = ioTDBSessionPoolManager.getSessionPool();
//        try {
//            // 构建查询语句
//            String sql = buildBatchWindowAggQueryCount(devices, points, startTime, endTime, windowSize, interval);
//            log.info("getQueryResultCountForMultipleDevices sql::{}", sql);
//
//            // 执行查询
//            try (SessionDataSetWrapper dataSetWrapper = session.executeQueryStatement(sql)) {
//                SessionDataSet resultSet = dataSetWrapper.getSessionDataSet();
//
//                // 由于是聚合查询，获取行数的方法是简单地调用 next()，不需要记录具体的字段
//                long count = 0;
//                while (resultSet.hasNext()) {
//                    resultSet.next();
//                    count++;
//                }
//                return count;
//            }
//        } catch (Exception ex) {
//            log.error("getQueryResultCountForMultipleDevices error, devices: {}, points: {}, startTime: {}, endTime: {}, windowSize: {}, interval: {}",
//                    devices, points, startTime, endTime, windowSize, interval, ex);
//            throw new RuntimeException(ex);
//        }
//    }
//
//    private static String buildBatchWindowAggQueryCount(List<String> devices, List<String> points, Long startTime, Long endTime, Integer windowSize, String interval) {
//        if (devices == null || devices.isEmpty() || points == null || points.isEmpty() || startTime == null || endTime == null || windowSize == null || interval == null) {
//            throw new IllegalArgumentException("Invalid input parameters");
//        }
//
//        StringBuilder queryBuilder = new StringBuilder();
//        queryBuilder.append("SELECT ");
//
//        // 构建SELECT部分，多个测点的平均值
//        for (int i = 0; i < points.size(); i++) {
//            queryBuilder.append("avg(").append(points.get(i)).append(")");
//            if (i < points.size() - 1) {
//                queryBuilder.append(", ");
//            }
//        }
//
//        // FROM部分，多个设备用逗号分隔
//        queryBuilder.append(" FROM ").append(String.join(", ", devices));
//
//        // GROUP BY部分
//        queryBuilder.append(" GROUP BY ([")
//                .append(startTime).append(", ")
//                .append(endTime).append("), ")
//                .append(windowSize).append(interval)
//                .append(") ");
//
//        // ORDER BY部分
//        queryBuilder.append("ORDER BY time ASC ");
//        return queryBuilder.toString();
//    }
//
//    private static String buildBatchWindowAggQuery(List<String> devices, List<String> points, Long startTime, Long endTime, Integer windowSize, String interval, int offset, int limit) {
//        if (devices == null || devices.isEmpty() || points == null || points.isEmpty() || startTime == null || endTime == null || windowSize == null || interval == null) {
//            throw new IllegalArgumentException("Invalid input parameters");
//        }
//
//        StringBuilder queryBuilder = new StringBuilder();
//        queryBuilder.append("SELECT ");
//
//        // 构建SELECT部分，多个测点的平均值
//        for (int i = 0; i < points.size(); i++) {
//            queryBuilder.append("avg(").append(points.get(i)).append(")");
//            if (i < points.size() - 1) {
//                queryBuilder.append(", ");
//            }
//        }
//
//        // FROM部分，多个设备用逗号分隔
//        queryBuilder.append(" FROM ").append(String.join(", ", devices));
//
//        // GROUP BY部分
//        queryBuilder.append(" GROUP BY ([")
//                .append(startTime).append(", ")
//                .append(endTime).append("), ")
//                .append(windowSize).append(interval)
//                .append(") ");
//
//        // ORDER BY部分
//        queryBuilder.append("ORDER BY time ASC fill(previous) ");
////        queryBuilder.append("ORDER BY time ASC ");
//
//        // LIMIT和OFFSET部分
//        queryBuilder.append("LIMIT ").append(limit).append(" OFFSET ").append(offset);
//
//        return queryBuilder.toString();
//    }
//
//    private String buildHistoryDataQuery(List<String> deviceIds, List<String> points, Long startTime, Long endTime, Integer offset, Integer limit) {
//        StringBuilder sqlBuilder = new StringBuilder("SELECT ");
//        for (int i = 0; i < points.size(); i++) {
//            sqlBuilder.append(points.get(i));
//            if (i < points.size() - 1) {
//                sqlBuilder.append(", ");
//            }
//        }
//        sqlBuilder.append(" FROM ");
//        for (int i = 0; i < deviceIds.size(); i++) {
//            sqlBuilder.append(deviceIds.get(i));
//            if (i < deviceIds.size() - 1) {
//                sqlBuilder.append(", ");
//            }
//        }
//        sqlBuilder.append(" WHERE 1=1 ");
//        sqlBuilder.append(" AND ");
//        if (startTime != null){
//            sqlBuilder.append("time >= ").append(startTime).append(" AND ");
//        }
//        if (endTime != null){
//            sqlBuilder.append("time <= ").append(endTime).append(" AND ");
//        }
//        sqlBuilder.append(" ORDER BY time ASC");
//        if (limit != null){
//            sqlBuilder.append(" LIMIT ").append(limit);
//        }
//        if (offset != null){
//            sqlBuilder.append(" OFFSET ").append(offset);
//        }
//        return sqlBuilder.toString();
//    }
//
//    /**
//     * 根据传入的测量值列表构建查询最新值语句
//     */
//    public static String buildLastDataQuery(List<String> deviceIds, List<String> points) {
//        StringBuilder sqlBuilder = new StringBuilder("select last ");
//        // 为每个数据点添加到 SELECT 子句
//        for (int i = 0; i < points.size(); i++) {
//            sqlBuilder.append(points.get(i));
//            if (i < points.size() - 1) {
//                sqlBuilder.append(", ");
//            }
//        }
//        // 添加 FROM 子句，包括所有设备
//        sqlBuilder.append(" from ");
//        for (int i = 0; i < deviceIds.size(); i++) {
//            sqlBuilder.append(deviceIds.get(i));
//            if (i < deviceIds.size() - 1) {
//                sqlBuilder.append(", ");
//            }
//        }
//        // 返回构建的 SQL 查询语句
//        return sqlBuilder.toString();
//    }
//
//    /**
//     * 根据传入的参数构建窗口聚合查询语句 降采样聚合查询
//     */
//    private static String buildWindowAggQuery(String device, List<String> points, Long startTime, Long endTime, Integer windowSize, String interval) {
//        StringBuilder queryBuilder = new StringBuilder();
//        queryBuilder.append("SELECT ");
//        for (int i = 0; i < points.size(); i++) {
//            queryBuilder.append("avg(").append(points.get(i)).append(") as avg_").append(points.get(i));
//            if (i < points.size() - 1) {
//                queryBuilder.append(", ");
//            }
//        }
//        queryBuilder.append(" FROM ").append(device);
//        queryBuilder.append(" GROUP BY ([").append(startTime).append(", ").append(endTime).append("), ").append(windowSize).append(interval);
//        queryBuilder.append(") ORDER BY time asc");
//        return queryBuilder.toString();
//    }
//
//    /**
//     * 根据传入的参数构建窗口聚合查询语句 降采样聚合查询
//     */
//    private static String buildWindowAggQuery(String device, List<String> points, Long startTime, Long endTime, Integer windowSize, String interval, int offset, int limit) {
//        StringBuilder queryBuilder = new StringBuilder();
//        queryBuilder.append("SELECT ");
//        for (int i = 0; i < points.size(); i++) {
//            queryBuilder.append("avg(").append(points.get(i)).append(") as avg_").append(points.get(i));
//            if (i < points.size() - 1) {
//                queryBuilder.append(", ");
//            }
//        }
//        queryBuilder.append(" FROM ").append(device);
//        queryBuilder.append(" GROUP BY ([").append(startTime).append(", ").append(endTime).append("), ").append(windowSize).append(interval);
//        queryBuilder.append(") ORDER BY time asc");
//        queryBuilder.append(" LIMIT ").append(limit).append(" OFFSET ").append(offset);
//        return queryBuilder.toString();
//    }
//
//    /**
//     * 获取指定查询返回的条数
//     *
//     * @param device    设备的唯一标识符
//     * @param points    要查询的数据点名称列表
//     * @param startTime 查询的开始时间戳
//     * @param endTime   查询的结束时间戳
//     * @return 查询返回的条数
//     */
//    public long getQueryResultCount(String device, List<String> points, Long startTime, Long endTime) {
//        SessionPool session = ioTDBSessionPoolManager.getSessionPool();
//        try {
//            // 构建查询语句
//            String sql = buildQueryResultCountSql(device, points, startTime, endTime);
//            log.info("getQueryResultCount sql::{}", sql);
//
//            // 执行查询
//            try (SessionDataSetWrapper dataSetWrapper = session.executeQueryStatement(sql)) {
//                SessionDataSet resultSet = dataSetWrapper.getSessionDataSet();
//
//                // 遍历结果集并计数
//                long count = 0;
//                while (resultSet.hasNext()) {
//                    resultSet.next();
//                    count++;
//                }
//                return count;
//            }
//        } catch (Exception ex) {
//            log.error("getQueryResultCount error, device: {}, points: {}, startTime: {}, endTime: {}",
//                    device, points, startTime, endTime, ex);
//            throw new RuntimeException(ex);
//        }
//    }
//
//    /**
//     * 根据传入的参数构建查询语句
//     */
//    private String buildQueryResultCountSql(String device, List<String> points, Long startTime, Long endTime) {
//        StringBuilder sqlBuilder = new StringBuilder("SELECT ");
//        for (int i = 0; i < points.size(); i++) {
//            sqlBuilder.append(points.get(i));
//            if (i < points.size() - 1) {
//                sqlBuilder.append(", ");
//            }
//        }
//        sqlBuilder.append(" FROM ").append(device);
//        sqlBuilder.append(" WHERE time >= ").append(startTime).append(" AND time <= ").append(endTime);
//        return sqlBuilder.toString();
//    }
//
//    /**
//     * 获取多设备指定查询返回的总条数
//     *
//     * @param deviceIds 设备的唯一标识符列表
//     * @param points    要查询的数据点名称列表
//     * @param startTime 查询的开始时间戳
//     * @param endTime   查询的结束时间戳
//     * @return 查询返回的总条数
//     */
//    public long getQueryResultCountForMultipleDevices(List<String> deviceIds, List<String> points, Long startTime, Long endTime) {
//        SessionPool session = ioTDBSessionPoolManager.getSessionPool();
//        try {
//            // 构建查询语句
//            String sql = buildQueryResultForMultipleDevices(deviceIds, points, startTime, endTime);
//            log.info("getQueryResultCountForMultipleDevices sql::{}", sql);
//
//            // 执行查询
//            try (SessionDataSetWrapper dataSetWrapper = session.executeQueryStatement(sql)) {
//                SessionDataSet resultSet = dataSetWrapper.getSessionDataSet();
//
//                // 遍历结果集并计数
//                long count = 0;
//                while (resultSet.hasNext()) {
//                    resultSet.next();
//                    count++;
//                }
//                return count;
//            }
//        } catch (Exception ex) {
//            log.error("getQueryResultCountForMultipleDevices error, deviceIds: {}, points: {}, startTime: {}, endTime: {}",
//                    deviceIds, points, startTime, endTime, ex);
//            throw new RuntimeException(ex);
//        }
//    }
//
//    /**
//     * 根据传入的参数构建多设备查询语句
//     */
//    private String buildQueryResultForMultipleDevices(List<String> deviceIds, List<String> points, Long startTime, Long endTime) {
//        StringBuilder sqlBuilder = new StringBuilder("SELECT ");
//        for (int i = 0; i < points.size(); i++) {
//            sqlBuilder.append(points.get(i));
//            if (i < points.size() - 1) {
//                sqlBuilder.append(", ");
//            }
//        }
//        sqlBuilder.append(" FROM ");
//        for (int i = 0; i < deviceIds.size(); i++) {
//            sqlBuilder.append(deviceIds.get(i));
//            if (i < deviceIds.size() - 1) {
//                sqlBuilder.append(", ");
//            }
//        }
//        sqlBuilder.append(" WHERE time >= ").append(startTime).append(" AND time <= ").append(endTime);
//        return sqlBuilder.toString();
//    }
//
//    /**
//     * 根据传入的测量值列表构建查询语句
//     */
//    private static String buildHistoryDataQuery(String device, List<String> points, Long startTime, Long endTime, int offset, int limit) {
//        StringBuilder queryBuilder = new StringBuilder();
//        queryBuilder.append("select ");
//        for (int i = 0; i < points.size(); i++) {
//            queryBuilder.append(points.get(i));
//            if (i < points.size() - 1) {
//                queryBuilder.append(", ");
//            }
//        }
//        queryBuilder.append(" from ").append(device);
//        queryBuilder.append(" where time >= ").append(startTime).append(" and time <= ").append(endTime);
//        queryBuilder.append(" order by time asc");
//        queryBuilder.append(" limit ").append(limit).append(" offset ").append(offset);
//        return queryBuilder.toString();
//    }
//
//    /**
//     * 根据传入的测量值列表构建查询语句
//     */
//    private static String buildAvgValueQuery(String device, List<String> points, Long startTime, Long endTime) {
//        StringBuilder queryBuilder = new StringBuilder();
//        queryBuilder.append("select ");
//
//        // 为每个测量值构建 AVG 聚合函数
//        for (int i = 0; i < points.size(); i++) {
//            queryBuilder.append("avg(").append(points.get(i)).append(")");
//            if (i < points.size() - 1) {
//                queryBuilder.append(", ");
//            }
//        }
//
//        // 指定查询的时间序列路径
//        queryBuilder.append(" from ").append(device);
//
//        // 时间范围条件（可根据需求调整）
//        if (Objects.nonNull(startTime) && Objects.nonNull(endTime)) {
//            queryBuilder.append(" where time >= ").append(startTime).append(" and time <= ").append(endTime);
//        }
//        return queryBuilder.toString();
//    }
//
//    /**
//     * 根据传入的测量值列表构建查询最大值和最小值语句
//     */
//    private static String buildStatisticsValueQuery(String device, List<String> points, Long startTime, Long
//            endTime) {
//        StringBuilder queryBuilder = new StringBuilder();
//        queryBuilder.append("select ");
//
//        // 为每个测量值构建 MAX 和 MIN 聚合函数
//        for (int i = 0; i < points.size(); i++) {
//            queryBuilder.append("max_value(").append(points.get(i)).append("), ").append("min_value(").append(points.get(i)).append(")");
//            if (i < points.size() - 1) {
//                queryBuilder.append(", ");
//            }
//        }
//
//        // 指定查询的时间序列路径
//        queryBuilder.append(" from ").append(device);
//        // 时间范围条件（可根据需求调整）
//        if (Objects.nonNull(startTime) && Objects.nonNull(endTime)) {
//            queryBuilder.append(" where time >= ").append(startTime).append(" and time <= ").append(endTime);
//        }
//        return queryBuilder.toString();
//    }
//
//    /**
//     * 根据传入的测量值列表构建求和查询语句
//     */
//    private static String buildSumValueQuery(String device, List<String> points, Long startTime, Long endTime) {
//        StringBuilder queryBuilder = new StringBuilder();
//        queryBuilder.append("select ");
//
//        // 为每个测量值构建 SUM 聚合函数
//        for (int i = 0; i < points.size(); i++) {
//            queryBuilder.append("sum(").append(points.get(i)).append(")");
//            if (i < points.size() - 1) {
//                queryBuilder.append(", ");
//            }
//        }
//
//        // 指定查询的时间序列路径
//        queryBuilder.append(" from ").append(device);
//        // 时间范围条件（可根据需求调整）
//        if (Objects.nonNull(startTime) && Objects.nonNull(endTime)) {
//            queryBuilder.append(" where time >= ").append(startTime).append(" and time <= ").append(endTime);
//        }
//        return queryBuilder.toString();
//    }
//}
