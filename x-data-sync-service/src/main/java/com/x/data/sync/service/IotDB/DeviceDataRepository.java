package com.x.data.sync.service.IotDB;

import com.x.data.sync.service.enums.IotValueType;
import com.x.dubbo.api.device.IDeviceInfoDubboService;
import com.x.repository.service.entity.DeviceInfoEntity;
import com.x.repository.service.entity.DevicePointInfoEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.iotdb.isession.pool.SessionDataSetWrapper;
import org.apache.iotdb.rpc.IoTDBConnectionException;
import org.apache.iotdb.rpc.StatementExecutionException;
import org.apache.iotdb.session.pool.SessionPool;
import org.apache.tsfile.enums.TSDataType;
import org.apache.tsfile.file.metadata.enums.CompressionType;
import org.apache.tsfile.file.metadata.enums.TSEncoding;
import org.apache.tsfile.read.common.RowRecord;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class DeviceDataRepository {
    private static final String STORAGE_GROUP = "root.iot"; // IotDB 存储组（类似数据库）

    // 注入 IotDB SessionPool
    private final SessionPool sessionPool;

    @DubboReference(version = "1.0.0")
    private IDeviceInfoDubboService deviceInfoDubboService;

    public DeviceDataRepository(SessionPool iotdbSessionPool) {
        this.sessionPool = iotdbSessionPool;
    }

    // 创建存储组（应用启动时执行一次）
    public void createStorageGroup() {
        try {
            // 直接执行创建语句
            sessionPool.createDatabase(STORAGE_GROUP);
            log.debug("IotDB 存储组 " + STORAGE_GROUP + " 创建成功！");
        } catch (Exception e) {
            if (e.getMessage().contains("root.iot")) {
                return; // 忽略该异常，正常退出
            }
            // 其他异常（如连接失败、权限问题）抛出
            throw new RuntimeException("创建 IotDB 存储组失败：" + e.getMessage(), e);
        }
    }

    // 注册时间序列
    public void createTimeSeries() {
        List<DeviceInfoEntity> list = deviceInfoDubboService.listContainsPoint().getData();
        if (ObjectUtils.isEmpty(list)) {
            log.warn("没有找到任何设备点信息，跳过创建时间序列");
            return;
        }
        for (DeviceInfoEntity deviceInfoEntity : list) {
            String deviceId = STORAGE_GROUP + "." + wrapNodeName("D_",deviceInfoEntity.getDeviceCode());
            List<String> measurements = new ArrayList<>(deviceInfoEntity.getPointList().stream().map(devicePointInfoEntity -> wrapNodeName("P_", devicePointInfoEntity.getId().toString())).toList());

            List<TSDataType> dataTypes = new ArrayList<>(deviceInfoEntity.getPointList().stream().map(devicePointInfoEntity -> TSDataType.valueOf(IotValueType.getDescByCode(devicePointInfoEntity.getPointType()))).toList());
            List<TSEncoding> encodings = new ArrayList<>(deviceInfoEntity.getPointList().stream().map(devicePointInfoEntity -> TSEncoding.PLAIN).toList());
            List<CompressionType> compressions = new ArrayList<>(deviceInfoEntity.getPointList().stream().map(devicePointInfoEntity -> CompressionType.UNCOMPRESSED).toList());
            List<String> tags = new ArrayList<>(deviceInfoEntity.getPointList().stream().map(DevicePointInfoEntity::getPointName).toList());
            try {
                // 创建时间序列
                sessionPool.createAlignedTimeseries(deviceId, measurements, dataTypes, encodings, compressions,tags);
                log.debug("IotDB 时间序列 {} 创建成功！", deviceId);
            } catch (Exception e) {
                if (!e.getMessage().contains("already exist")) {
                    log.error("注册时间序列异常：",e);
                }
            }
        }
    }


    private String wrapNodeName(String per,String nodeName) {
        return per+nodeName;
    }

    // 写入数据
    public void insertData(DeviceInfoEntity deviceInfoEntity) {
        // 验证参数
        if (ObjectUtils.isEmpty(deviceInfoEntity) || ObjectUtils.isEmpty(deviceInfoEntity.getPointList())) {
            throw new IllegalArgumentException("设备和点位均不能为空");
        }

        String deviceId = STORAGE_GROUP + "." + wrapNodeName("D_", deviceInfoEntity.getDeviceCode());

        // 时间戳和值处理
        long timestamp = deviceInfoEntity.getTimestamp() == null ? System.currentTimeMillis() : deviceInfoEntity.getTimestamp();

        List<String> measurements = deviceInfoEntity.getPointList().stream().map(devicePointInfoEntity -> wrapNodeName("P_", devicePointInfoEntity.getId().toString())).toList();

        List<TSDataType> dataTypes = deviceInfoEntity.getPointList().stream().map(devicePointInfoEntity -> TSDataType.valueOf(IotValueType.getDescByCode(devicePointInfoEntity.getPointType()))).toList();
        
        List<Object> values = deviceInfoEntity.getPointList().stream().map(DevicePointInfoEntity::getPointValue).toList();

        try {
            sessionPool.insertAlignedRecord(deviceId, timestamp, measurements,dataTypes,values);
        } catch (Exception e) {
            throw new RuntimeException("写入时序数据失败：" + deviceId + "，原因：" + e.getMessage(), e);
        }
    }

    // 批量写入数据
    public void insertDeviceData(List<DeviceInfoEntity> dataList) {
        if (ObjectUtils.isEmpty(dataList)) {
            return;
        }
        List<String> deviceIds = new ArrayList<>();
        List<Long> timestamps = new ArrayList<>();
        List<List<String>> measurementsList = new ArrayList<>();
        List<List<TSDataType>> dataTypesList = new ArrayList<>();
        List<List<Object>> valuesList = new ArrayList<>();

        for (DeviceInfoEntity deviceInfoEntity : dataList) {
            String deviceId = STORAGE_GROUP + "." + wrapNodeName("D_", deviceInfoEntity.getDeviceCode());
            deviceIds.add(deviceId);
            long timestamp = deviceInfoEntity.getTimestamp() == null ? System.currentTimeMillis() : deviceInfoEntity.getTimestamp();
            timestamps.add(timestamp);
            List<String> measurements = deviceInfoEntity.getPointList().stream().map(devicePointInfoEntity -> wrapNodeName("P_", devicePointInfoEntity.getId().toString())).toList();
            measurementsList.add(measurements);
            List<TSDataType> dataTypes = deviceInfoEntity.getPointList().stream().map(devicePointInfoEntity -> TSDataType.valueOf(IotValueType.getDescByCode(devicePointInfoEntity.getPointType()))).toList();
            dataTypesList.add(dataTypes);
            List<Object> values = deviceInfoEntity.getPointList().stream().map(devicePointInfoEntity -> {
                if (devicePointInfoEntity.getPointValue() instanceof BigDecimal) {
                    return ((BigDecimal) devicePointInfoEntity.getPointValue()).floatValue();
                }
                return devicePointInfoEntity.getPointValue();
            }).toList();
            valuesList.add(values);
        }

        try {
            sessionPool.insertAlignedRecords(deviceIds, timestamps, measurementsList,dataTypesList,valuesList);
        } catch (Exception e) {
            throw new RuntimeException("批量写入时序数据失败，原因：" + e.getMessage(), e);
        }
    }

    // 查询最新数据
    public DevicePointInfoEntity queryLatestData(String deviceCode, Long pointId) {
        String deviceNode = wrapNodeName("D_", deviceCode);
        String pointNode = wrapNodeName("P_", pointId.toString());
        
        String sql = String.format(
            "SELECT %s as metric_val FROM %s.%s ORDER BY time DESC LIMIT 1",
            pointNode,
            STORAGE_GROUP,
            deviceNode
        );

        log.info("查询最新数据SQL：{}", sql);

        try {
            RowRecord record = executeQueryAndGetFirstRecord(sql);
            if (record == null) {
                throw new RuntimeException(String.format("设备 [%s] 指标 [%s] 未查询到时序数据", deviceCode, pointId));
            }
            
            DevicePointInfoEntity entity = new DevicePointInfoEntity();
            entity.setDeviceCode(deviceCode);
            entity.setId(pointId);
            entity.setPointValue(getValueFromRecord(record));
            entity.setTimestamp(record.getTimestamp());
            return entity;
        } catch (Exception e) {
            throw new RuntimeException(String.format("查询时序数据失败：SQL=%s, 原因=%s", sql, e.getMessage()), e);
        }
    }

    // 查询历史数据（时间范围）
    public List<DevicePointInfoEntity> queryHistoryData(String deviceCode, Long pointId, String startTime, String endTime) {
        // 入参校验（避免非法参数导致 SQL 语法错误）
        if (deviceCode == null || pointId == null || startTime == null || endTime == null) {
            throw new IllegalArgumentException("设备ID、指标名、开始时间、结束时间均不能为空");
        }

        String deviceNode = wrapNodeName("D_", deviceCode);
        String pointNode = wrapNodeName("P_", pointId.toString());
        
        long startTimestamp = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endTimestamp = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();

        String sql = String.format(
            "SELECT %s as metric_val FROM %s.%s WHERE time >= %d AND time <= %d ORDER BY time ASC",
            pointNode,
            STORAGE_GROUP,
            deviceNode,
            startTimestamp,
            endTimestamp
        );

        log.info("查询历史数据SQL：{}", sql);

        List<DevicePointInfoEntity> result = new ArrayList<>();
        try {
            SessionDataSetWrapper sessionDataSetWrapper = sessionPool.executeQueryStatement(sql);
            while (sessionDataSetWrapper.hasNext()) {
                RowRecord record = sessionDataSetWrapper.next();
                DevicePointInfoEntity entity = new DevicePointInfoEntity();
                entity.setDeviceCode(deviceCode);
                entity.setId(pointId);
                entity.setPointValue(getValueFromRecord(record));
                entity.setTimestamp(record.getTimestamp());
                result.add(entity);
            }
            sessionDataSetWrapper.close();
        } catch (IoTDBConnectionException | StatementExecutionException e) {
            throw new RuntimeException(
                String.format(
                    "查询历史时序数据失败：deviceCode=%s, pointId=%s, 时间范围=[%s, %s], SQL=%s, 原因=%s",
                        deviceCode, pointId, startTime, endTime, sql, e.getMessage()
                ),
                e
            );
        }

        return result;
    }
    
    private RowRecord executeQueryAndGetFirstRecord(String sql) throws IoTDBConnectionException, StatementExecutionException {
        SessionDataSetWrapper sessionDataSetWrapper = sessionPool.executeQueryStatement(sql);
        RowRecord record = null;
        if (sessionDataSetWrapper.hasNext()) {
            record = sessionDataSetWrapper.next();
        }
        sessionDataSetWrapper.close();
        return record;
    }
    
    private Object getValueFromRecord(RowRecord record) {
        return record.getFields().get(0).getObjectValue(TSDataType.FLOAT);
    }
}