package com.x.data.collection.service.channel.iotdb.service;

import com.x.data.collection.service.channel.iotdb.dto.*;
import org.apache.iotdb.tsfile.file.metadata.enums.TSDataType;

import java.util.List;

public interface IotDBService {

    /**
     * 根据设备获取指标实时数据
     *
     * @param db     iotdb库名
     * @param device iotdb设备路径
     * @param points iotdb测点编码
     * @return
     */
    List<RealtimeValueDto> realtime(String db, String device, List<String> points);

    /**
     * 根据设备获取指标点码平均值
     *
     * @param device    iotdb设备路径 用点隔开
     * @param points    iotdb设备路径 用点隔开
     * @param startTime 开始时间 秒级时间戳
     * @param endTime   结束时间 秒级时间戳
     * @return
     */
    List<AvgValueDto> avgValue(String device, List<String> points, Long startTime, Long endTime);

    /**
     * 根据设备获取指标点码最大值和最小值
     *
     * @param device    iotdb设备路径 用点隔开
     * @param points    iotdb设备路径 用点隔开
     * @param startTime 开始时间 秒级时间戳
     * @param endTime   结束时间 秒级时间戳
     * @return
     */
    List<StatisticsValueDto> statisticsValue(String device, List<String> points, Long startTime, Long endTime);

    /**
     * 根据设备求和指标点码
     *
     * @param device
     * @param points
     * @return
     */
    List<SumValueDto> sumValue(String device, List<String> points, Long startTime, Long endTime);

    /**
     * 获取点码历史数据
     *
     * @param device
     * @param: points
     * @param: page
     * @param: pageSize
     * @param: startTime
     * @param: endTime
     */
    HistoryValuesDto historyData(String device, List<String> points, Integer page, Integer pageSize, Long startTime, Long endTime);

    /**
     * 根据时间窗口按照时间间隔获取点码历史数据
     *
     * @param device
     * @param: points
     * @param: page
     * @param: pageSize
     * @param: startTime
     * @param: endTime
     * @param: windowSize 间隔窗口大小
     */
    HistoryValuesDto windowAggHistoryData(String device, List<String> points, Integer page, Integer pageSize, Long startTime, Long endTime, Integer windowSize, String interval);

    /**
     * 插入数据到IoTDB
     *
     * @param device   deviceId: String - 设备的唯一标识符。
     * @param points   pointArr: String[] - 要插入的数据点名称列表。
     * @param types    types: TSDataType[] - 各数据点对应的数据类型列表,支持的数据类型(BOOLEAN,INT32,INT64,FLOAT,DOUBLE,TEXT)。
     * @param dataJson dataJson: String - 包含要插入的数据的JSON字符串。
     *                 {"data":[{"timestamp":1617187200,"temperature":"111","speed":"1"},{"timestamp":1617187200,"temperature":"111","speed":"1"}]}
     *                 timestamp: Long (可选) - 数据点的时间戳，单位为毫秒。如果不传，则默认为当前时间。
     */
    int insertData(String device, List<String> points, List<TSDataType> types, String dataJson);

    /**
     * 多设备获取指标实时数据
     *
     * @param deviceIds iotdb设备路径
     * @param points    iotdb测点编码
     * @return
     */
    List<BatchRealtimeValueDto> batchRealtime(List<String> deviceIds, List<String> points);

    /**
     * 多设备获取指标点码历史数据
     *
     * @param deviceIds iotdb设备路径
     * @param points    iotdb测点编码
     * @return
     */
    BatchHistoryValuesDto batchHistoryData(List<String> deviceIds, List<String> points, Integer page, Integer pageSize, Long startTime, Long endTime);

    /**
     * 根据时间窗口按照时间间隔获取点码历史数据
     *
     * @param deviceIds
     * @param: points
     * @param: page
     * @param: pageSize
     * @param: startTime
     * @param: endTime
     * @param: windowSize 间隔窗口大小
     */
    BatchHistoryValuesDto batchWindowAggHistoryData(List<String> deviceIds, List<String> points, Integer page, Integer pageSize, Long startTime, Long endTime, Integer windowSize, String interval);

    /**
     * 根据设备获取指标点码的条件数据
     *
     * @param deviceIds iotdb设备ID列表
     * @param points    iotdb测点编码
     * @param startTime 开始时间（秒级时间戳）
     * @param endTime   结束时间（秒级时间戳）
     * @param sql       查询条件SQL片段
     * @return 设备条件数据列表
     */
    List<DeviceConditionDto> conditionDevice(List<String> deviceIds, List<String> points, Long startTime, Long endTime, String sql);

}
