package com.x.data.sync.service.IotDB;

import com.x.data.sync.service.enums.IotValueType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * IotDB 数据读写
 *
 * @author whj
 */
@Slf4j
@Repository
public class DeviceDataRepository {
    private static final String STORAGE_GROUP = "root.iot"; // IotDB 存储组（类似数据库）

    // 注入 IotDB 专属 JdbcTemplate（通过名称匹配）
    private final JdbcTemplate jdbcTemplate;

    public DeviceDataRepository(@Qualifier("iotdbJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 创建存储组（应用启动时执行一次）
    public void createStorageGroup() {
        String createSql = String.format("CREATE STORAGE GROUP %s", STORAGE_GROUP);
        try {
            // 直接执行创建语句
            jdbcTemplate.execute(createSql);
            System.out.println("IotDB 存储组 " + STORAGE_GROUP + " 创建成功！");
        } catch (Exception e) {
            if (e.getMessage().contains("root.iot")) {
                return; // 忽略该异常，正常退出
            }
            // 其他异常（如连接失败、权限问题）抛出
            throw new RuntimeException("创建 IotDB 存储组失败：" + e.getMessage(), e);
        }
    }

    // 注册时间序列
    public void createTimeSeries(DevicePointData data) {
        String tsPath = String.format(
                "%s.%s.%s",
                STORAGE_GROUP,
                wrapNodeName("D_",data.getDeviceId().toString()), // 设备ID是数字，无需包裹，但统一调用方法更规范
                wrapNodeName("P_",data.getId().toString())
        );
        String dataType = IotValueType.getDescByCode(data.getPointType());
        String createSql = String.format(
                "create timeseries %s with datatype=%s,encoding=PLAIN",
                tsPath,
                dataType
        );

        try {
            jdbcTemplate.execute(createSql);
            System.out.println("IotDB 时间序列 " + tsPath + " 创建成功！");
        } catch (Exception e) {
            if (!e.getMessage().contains("already exist")) {
                log.error("注册时间序列异常：",e);
            }
        }
    }


    private String wrapNodeName(String per,String nodeName) {
        return per+nodeName;
    }

    // 写入数据
    public void insertData(DevicePointData data) {
        // 验证参数
        if (data.getDeviceId() == null || data.getId() == null || data.getPointValue() == null) {
            throw new IllegalArgumentException("设备ID、监测点、值均不能为空");
        }

        // 构建设备路径
        String devicePath = String.format(
                "%s.%s",
                STORAGE_GROUP,
                wrapNodeName("D_",data.getDeviceId().toString())
        );
        // 构建监测点路径
        String pointPath = wrapNodeName("P_",data.getId().toString());

        // 时间戳和值处理
        Long timestamp = data.getTimestamp() == null ? System.currentTimeMillis() : data.getTimestamp();
        Object pointValue = data.getPointValue();
        String valueStr = buildValueString(pointValue); // 简化：假设是FLOAT类型，无需判断pointType

        // 拼接写入SQL（路径带引号）
        String sql = String.format(
                "insert into %s(time, %s) values(%d, %s)",
                devicePath, pointPath, timestamp, valueStr
        );

        try {
            jdbcTemplate.execute(sql);
            System.out.println("时序数据写入成功：" + sql);
        } catch (Exception e) {
            throw new RuntimeException("写入时序数据失败：" + sql + "，原因：" + e.getMessage(), e);
        }
    }

    private String buildValueString(Object value) {
        return value.toString();
    }

    // 查询最新数据
    public DevicePointData queryLatestData(Long deviceId, Long pointId) {
        // 构建查询 SQL（核心：给 LAST_VALUE 结果加别名 metric_val）
        String querySql = String.format(
                "select " + wrapNodeName("P_",pointId.toString()) +
                        " as metric_val from "+STORAGE_GROUP+".%s " +
                        "order by time desc limit 1", // 2.0.5 用 LIMIT 1 取最新，兼容 LAST_VALUE
                wrapNodeName("D_",deviceId.toString())
        );

        log.info("查询最新数据SQL：{}", querySql);

        try {
            // 执行查询，传入自定义 RowMapper
            return jdbcTemplate.queryForObject(querySql,new DeviceDataRowMapper(deviceId, pointId));
        } catch (EmptyResultDataAccessException e) {
            throw new RuntimeException(String.format("设备 [%d] 指标 [%s] 未查询到时序数据", deviceId, pointId), e);
        } catch (Exception e) {
            throw new RuntimeException(String.format("查询时序数据失败：SQL=%s, 原因=%s", querySql, e.getMessage()), e);
        }
    }

    // 查询历史数据（时间范围）
    public List<DevicePointData> queryHistoryData(Long deviceId, Long pointId, String startTime, String endTime) {
        // 入参校验（避免非法参数导致 SQL 语法错误）
        if (deviceId == null || pointId == null || startTime == null || endTime == null) {
            throw new IllegalArgumentException("设备ID、指标名、开始时间、结束时间均不能为空");
        }

        //处理设备路径
        String legalDeviceNode = wrapNodeName("D_",deviceId.toString());
        String devicePath = String.format("%s.%s", STORAGE_GROUP, legalDeviceNode); // 如：root.iot.1001

        //构建 SQL（无占位符、加列别名、时间范围拼接）
        String sql = String.format(
                "SELECT " + wrapNodeName("P_",pointId.toString()) +
                        " as metric_val FROM " + devicePath +
                " WHERE time >= %s AND time <= %s " + // 直接拼接 Long 类型时间戳，无注入风险
                        "ORDER BY time ASC",
                LocalDateTime.parse(startTime,DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),       // 开始时间
                LocalDateTime.parse(endTime,DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))          // 结束时间
        );

        log.info("查询历史数据SQL：{}", sql);

        try {
            //执行查询
            return jdbcTemplate.query(sql, new DeviceDataRowMapper(deviceId, pointId));
        } catch (Exception e) {
            //增强异常信息：包含完整 SQL、参数，快速定位问题
            throw new RuntimeException(
                    String.format(
                            "查询历史时序数据失败：deviceId=%d, pointId=%s, 时间范围=[%s, %s], SQL=%s, 原因=%s",
                            deviceId, pointId, startTime, endTime, sql, e.getMessage()
                    ),
                    e
            );
        }
    }
}