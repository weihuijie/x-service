package com.x.offline.analysis.service.service;

import com.x.common.utils.DateTimeUtils;
import com.x.offline.analysis.service.config.IoTDBConfig;
import com.x.offline.analysis.service.utils.HdfsUtil;
import com.x.offline.analysis.service.utils.SparkUtil;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static org.apache.spark.sql.functions.*;

/**
 *
 *  核心服务
 *
 * @author whj
 */
@Slf4j
@Service
public class ReportService {
    private final SparkUtil sparkUtil;
    private final HdfsUtil hdfsUtil;

    private final IoTDBConfig iotdbConfig;

    public ReportService(SparkUtil sparkUtil, HdfsUtil hdfsUtil, IoTDBConfig iotdbConfig) {
        this.sparkUtil = sparkUtil;
        this.hdfsUtil = hdfsUtil;
        this.iotdbConfig = iotdbConfig;
    }

    /**
     * 生成日报表（指定日期的全量设备数据统计）
     */
    @XxlJob("spark-generateDailyReport")
    public void generateDailyReport(String date) throws IOException {
        date = "2025-11-12";
        try {
            SparkSession spark = sparkUtil.getSparkSession();
            // 1. 计算时间范围（当天 00:00:00 到 23:59:59.999）
            String startTime = date + " 00:00:00.000";
            String endTime = date + " 23:59:59.999";
            log.info("生成日报表，时间范围：{} - {}", startTime, endTime);

            // 2. 转换为时间戳
            long startTimestamp = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
                    .atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
            long endTimestamp = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
                    .atZone(ZoneOffset.UTC).toInstant().toEpochMilli();

            // 3. 构造SQL查询语句（使用时间戳）
            String sql = String.format("SELECT * FROM %s WHERE Time >= %d AND Time <= %d", 
                    iotdbConfig.getTablePath(), startTimestamp, endTimestamp);
            
            // 4. 读取 IoTDB 全量设备数据
            Dataset<Row> iotData = spark.read()
                    .format("org.apache.iotdb.spark.db")
                    .option("url", iotdbConfig.getUrl())
                    .option("user", iotdbConfig.getUser())
                    .option("password", iotdbConfig.getPassword())
                    .option("sql", sql)
                    .load();

            // 5. 数据预处理（提取日期、设备ID等维度）
            Dataset<Row> processedData = iotData
                    .withColumn("date", to_date(col("Time"), "yyyy-MM-dd"));
                    
            // 动态添加device_id列（从列名中提取设备ID）
            String[] columnNames = processedData.columns();
            String p4Column = null;
            String p3Column = null;
            String p5Column = null;
            String p6Column = null;
            String p8Column = null;
            String p7Column = null;
            
            // 查找各个数据列的实际名称
            for (String colName : columnNames) {
                if (colName.endsWith(".P_4")) {
                    p4Column = colName;
                } else if (colName.endsWith(".P_3")) {
                    p3Column = colName;
                } else if (colName.endsWith(".P_5")) {
                    p5Column = colName;
                } else if (colName.endsWith(".P_6")) {
                    p6Column = colName;
                } else if (colName.endsWith(".P_8")) {
                    p8Column = colName;
                } else if (colName.endsWith(".P_7")) {
                    p7Column = colName;
                }
            }
            
            if (p4Column != null && p3Column != null) {
                // 从列名中提取设备ID
                String deviceId = p4Column.replaceAll("root\\.iot\\.([^\\.]+)\\..*", "$1");
                processedData = processedData.withColumn("device_id", lit(deviceId));
                
                // 6. 日报表统计（按设备ID+日期分组，统计各测点指标）
                Dataset<Row> dailyReport = processedData
                        .groupBy("device_id", "date")
                        .agg(
                                avg("`" + p4Column + "`").alias("P_4_avg"), // 均值
                                max("`" + p3Column + "`").alias("P_3_max"), // 最大值
                                min("`" + p3Column + "`").alias("P_3_min"), // 最小值
                                sum(when(col("`" + p3Column + "`").gt(50), 1).otherwise(0)).alias("P_3_gt50_count"), // 超阈值数量
                                count("*").alias("total_point_count") // 总测点数量
                        );

                // 7. 保存到 HDFS（Parquet 格式，压缩率高）
                hdfsUtil.saveReport(dailyReport, "daily/" + date, "parquet",1);
            } else {
                log.error("无法找到必要的数据列 P_4 和 P_3");
            }
        } catch (Exception e) {
            log.error("生成报表失败", e);
        }
        finally {
            sparkUtil.stopSparkSession();
        }
    }

    /**
     * 生成月报表（指定月份的全量设备数据统计）
     */
    @XxlJob("spark-generateMonthlyReport")
    public void generateMonthlyReport(String month) throws IOException {
        SparkSession spark = sparkUtil.getSparkSession();
        try {
            // 1. 计算时间范围（当月1号 00:00:00 到当月最后一天 23:59:59.999）
            String startTime = month + "-01 00:00:00.000";
            String endTime = DateTimeUtils.getLastDayOfMonth(month) + " 23:59:59.999";
            log.info("生成月报表，时间范围：{} - {}", startTime, endTime);

            // 2. 转换为时间戳
            long startTimestamp = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
                    .atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
            long endTimestamp = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
                    .atZone(ZoneOffset.UTC).toInstant().toEpochMilli();

            // 3. 构造SQL查询语句（使用时间戳）
            String sql = String.format("SELECT * FROM %s WHERE Time >= %d AND Time <= %d", 
                    iotdbConfig.getTablePath(), startTimestamp, endTimestamp);

            // 4. 读取 IoTDB 数据（同日报逻辑）
            Dataset<Row> iotData = spark.read()
                    .format("org.apache.iotdb.spark.db")
                    .option("url", iotdbConfig.getUrl())
                    .option("user", iotdbConfig.getUser())
                    .option("password", iotdbConfig.getPassword())
                    .option("sql", sql)
                    .load();

            // 5. 预处理（提取月份、设备ID）
            Dataset<Row> processedData = iotData
                    .withColumn("month", date_format(col("Time"), "yyyy-MM"));
                    
            // 动态添加device_id列（从列名中提取设备ID）
            String[] columnNames = processedData.columns();
            String p4Column = null;
            String p3Column = null;
            String p5Column = null;
            String p6Column = null;
            String p8Column = null;
            String p7Column = null;
            
            // 查找各个数据列的实际名称
            for (String colName : columnNames) {
                if (colName.endsWith(".P_4")) {
                    p4Column = colName;
                } else if (colName.endsWith(".P_3")) {
                    p3Column = colName;
                } else if (colName.endsWith(".P_5")) {
                    p5Column = colName;
                } else if (colName.endsWith(".P_6")) {
                    p6Column = colName;
                } else if (colName.endsWith(".P_8")) {
                    p8Column = colName;
                } else if (colName.endsWith(".P_7")) {
                    p7Column = colName;
                }
            }
            
            if (p4Column != null && p3Column != null && p5Column != null && p6Column != null && p8Column != null) {
                // 从列名中提取设备ID
                String deviceId = p4Column.replaceAll("root\\.iot\\.([^\\.]+)\\..*", "$1");
                processedData = processedData.withColumn("device_id", lit(deviceId));
                
                // 6. 月报表统计（按设备ID+月份分组）
                Dataset<Row> monthlyReport = processedData
                        .groupBy("device_id", "month")
                        .agg(
                                avg("`" + p8Column + "`").alias("P_8_avg"),
                                sum("`" + p5Column + "`").alias("P_5_total"),
                                countDistinct("date").alias("active_days"), // 设备活跃天数
                                max(when(col("`" + p6Column + "`").equalTo(true), col("Time"))).alias("last_active_time") // 最后活跃时间
                        );

                // 7. 保存到 HDFS
                hdfsUtil.saveReport(monthlyReport, "monthly/" + month, "parquet",1);
            } else {
                log.error("无法找到必要的数据列");
            }
        } finally {
            sparkUtil.stopSparkSession();
        }
    }
}