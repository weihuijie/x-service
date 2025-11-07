//package com.x.data.collection.service.channel.iotdb.controller;
//
//import com.x.common.base.R;
//import com.x.data.collection.service.channel.iotdb.dto.IotDbInsertBeanDto;
//import com.x.data.collection.service.channel.iotdb.service.IotDBService;
//import com.x.data.collection.service.channel.iotdb.vo.IotDbBatchHistoryRequestQueryVo;
//import com.x.data.collection.service.channel.iotdb.vo.IotDbBatchRequestQueryVo;
//import com.x.data.collection.service.channel.iotdb.vo.IotDbRequestQueryVo;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.http.util.Asserts;
//import org.springframework.http.MediaType;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@Slf4j
//@RequestMapping("/data/collection/iotdb")
//public class IotDBApiController {
//
//    final IotDBService iotDBService;
//
//    public IotDBApiController(IotDBService iotDBService) {
//        this.iotDBService = iotDBService;
//    }
//
//    @PostMapping(value = "/condition/device/ids", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
//    public R<?> conditionDevice(@RequestBody IotDbRequestQueryVo request) {
//        log.info("获取设备点位数据::{}", request);
//        try {
//            Asserts.notNull(request, "参数为空");
//            Asserts.notNull(request.getDeviceIds(), "设备为空");
//            Asserts.notNull(request.getPoints(), "测点编码为空");
//            return R.data(iotDBService.conditionDevice(request.getDeviceIds(), request.getPoints(), request.getStartTime(),request.getEndTime(),request.getSql()));
//        } catch (Exception e) {
//            return R.fail(e.getMessage());
//        }
//    }
//
//    @PostMapping(value = "/realtime", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
//    public R<?> realtime(@RequestBody IotDbRequestQueryVo request) {
//        log.info("获取点码实时数据::{}", request);
//        try {
//            Asserts.notNull(request, "参数为空");
//            Asserts.notNull(request.getDb(), "数据库为空");
//            Asserts.notNull(request.getDevice(), "设备路径为空");
//            Asserts.notNull(request.getPoints(), "测点编码为空");
//            return R.data(iotDBService.realtime(request.getDb(), request.getDevice(), request.getPoints()));
//        } catch (Exception e) {
//            return R.fail(e.getMessage());
//        }
//    }
//
//    @PostMapping(value = "/avgValue", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
//    public R<?> avgValue(@RequestBody IotDbRequestQueryVo request) {
//        log.info("获取点码平均数据::{}", request);
//        try {
//            Asserts.notNull(request, "参数为空");
//            Asserts.notNull(request.getDb(), "数据库为空");
//            Asserts.notNull(request.getDevice(), "设备路径为空");
//            Asserts.notNull(request.getPoints(), "测点编码为空");
//            return R.data(iotDBService.avgValue(request.getDevice(), request.getPoints(), request.getStartTime(), request.getEndTime()));
//        } catch (Exception e) {
//            return R.fail(e.getMessage());
//        }
//    }
//
//    @PostMapping(value = "/statisticsValue", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
//    public R<?> statisticsValue(@RequestBody IotDbRequestQueryVo request) {
//        log.info("获取点码最大最小值数据::{}", request);
//        try {
//            Asserts.notNull(request, "参数为空");
//            Asserts.notNull(request.getDevice(), "设备路径为空");
//            Asserts.notNull(request.getPoints(), "测点编码为空");
//            return R.data(iotDBService.statisticsValue(request.getDevice(), request.getPoints(), request.getStartTime(), request.getEndTime()));
//        } catch (Exception e) {
//            return R.fail(e.getMessage());
//        }
//    }
//
//    @PostMapping(value = "/sumValue", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
//    public R<?> sumValue(@RequestBody IotDbRequestQueryVo request) {
//        log.info("获取点码求和数据::{}", request);
//        try {
//            Asserts.notNull(request, "参数为空");
//            Asserts.notNull(request.getDevice(), "设备路径为空");
//            Asserts.notNull(request.getPoints(), "测点编码为空");
//            return R.data(iotDBService.sumValue(request.getDevice(), request.getPoints(), request.getStartTime(), request.getEndTime()));
//        } catch (Exception e) {
//            return R.fail(e.getMessage());
//        }
//    }
//
//    @PostMapping(value = "/history", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
//    public R<?> history(@RequestBody IotDbRequestQueryVo request) {
//        log.info("获取点码历史数据::{}", request);
//        try {
//            Asserts.notNull(request, "参数为空");
//            Asserts.notNull(request.getDevice(), "设备路径为空");
//            Asserts.notNull(request.getPoints(), "测点编码为空");
//            Asserts.notNull(request.getStartTime(), "开始时间为空");
//            Asserts.notNull(request.getEndTime(), "结束时间为空");
//            Asserts.notNull(request.getPage(), "分页为空");
//            Asserts.notNull(request.getPageSize(), "页大小为空");
//            return R.data(iotDBService.historyData(request.getDevice(), request.getPoints(), request.getPage(), request.getPageSize(), request.getStartTime(), request.getEndTime()));
//        } catch (Exception e) {
//            return R.fail(e.getMessage());
//        }
//    }
//
//    @PostMapping(value = "/windowAgg", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
//    public R<?> windowAgg(@RequestBody IotDbRequestQueryVo request) {
//        log.info("根据时间窗口按照时间间隔获取点码历史数据::{}", request);
//        try {
//            Asserts.notNull(request, "参数为空");
//            Asserts.notNull(request.getDevice(), "设备路径为空");
//            Asserts.notNull(request.getPoints(), "测点编码为空");
//            Asserts.notNull(request.getStartTime(), "开始时间为空");
//            Asserts.notNull(request.getEndTime(), "结束时间为空");
//            Asserts.notNull(request.getPage(), "分页为空");
//            Asserts.notNull(request.getPageSize(), "页大小为空");
//            return R.data(iotDBService.windowAggHistoryData(request.getDevice(), request.getPoints(), request.getPage(), request.getPageSize(), request.getStartTime(), request.getEndTime(), request.getWindowSize(), request.getInterval()));
//        } catch (Exception e) {
//            return R.fail(e.getMessage());
//        }
//    }
//
//    @PostMapping(value = "/batchInsert", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
//    public R<?> batchInsert(@RequestBody IotDbInsertBeanDto insertBean) {
////        log.info("批量插入数据::{}", insertBean);
//        try {
//            Asserts.notNull(insertBean, "参数为空");
//            Asserts.notNull(insertBean.getDeviceId(), "设备路径为空");
//            Asserts.notNull(insertBean.getPointArr(), "测点编码为空");
//            Asserts.notNull(insertBean.getTypes(), "数据类型为空");
//            //成功插入的条数
//            int total = iotDBService.insertData(insertBean.getDeviceId(), insertBean.getPointArr(), insertBean.getTypes(), insertBean.getDataJson());
//            return R.data(total);
//        } catch (Exception e) {
//            return R.fail(e.getMessage());
//        }
//    }
//
//    @PostMapping(value = "/batch/realtime", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
//    public R<?> batchRealtime(@RequestBody IotDbBatchRequestQueryVo request) {
//        log.info("获取点码实时数据::{}", request);
//        try {
//            Asserts.notNull(request, "参数为空");
//            Asserts.notNull(request.getDeviceIds(), "设备路径为空");
//            Asserts.notNull(request.getPoints(), "测点编码为空");
//            return R.data(iotDBService.batchRealtime(request.getDeviceIds(), request.getPoints()));
//        } catch (Exception e) {
//            return R.fail(e.getMessage());
//        }
//    }
//
//    @PostMapping(value = "/batch/history", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
//    public R<?> batchHistory(@RequestBody IotDbBatchHistoryRequestQueryVo request) {
//        log.info("获取点码历史数据::{}", request);
//        try {
//            Asserts.notNull(request, "参数为空");
//            Asserts.notNull(request.getDeviceIds(), "设备路径为空");
//            Asserts.notNull(request.getPoints(), "测点编码为空");
//            Asserts.notNull(request.getStartTime(), "开始时间为空");
//            Asserts.notNull(request.getEndTime(), "结束时间为空");
//            Asserts.notNull(request.getPage(), "分页为空");
//            Asserts.notNull(request.getPageSize(), "页大小为空");
//            return R.data(iotDBService.batchHistoryData(request.getDeviceIds(), request.getPoints(), request.getPage(), request.getPageSize(), request.getStartTime(), request.getEndTime()));
//        } catch (Exception e) {
//            return R.fail(e.getMessage());
//        }
//    }
//
//
//    @PostMapping(value = "/batch/windowAgg", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
//    public R<?> batchWindowAgg(@RequestBody IotDbBatchHistoryRequestQueryVo request) {
//        log.info("多设备多测点根据时间窗口按照时间间隔获取点码历史数据::{}", request);
//        try {
//            Asserts.notNull(request, "参数为空");
//            Asserts.notNull(request.getDeviceIds(), "设备路径为空");
//            Asserts.notNull(request.getPoints(), "测点编码为空");
//            Asserts.notNull(request.getStartTime(), "开始时间为空");
//            Asserts.notNull(request.getEndTime(), "结束时间为空");
//            Asserts.notNull(request.getPage(), "分页为空");
//            Asserts.notNull(request.getPageSize(), "页大小为空");
//            return R.data(iotDBService.batchWindowAggHistoryData(request.getDeviceIds(), request.getPoints(), request.getPage(), request.getPageSize(), request.getStartTime(), request.getEndTime(), request.getWindowSize(), request.getInterval()));
//        } catch (Exception e) {
//            return R.fail(e.getMessage());
//        }
//    }
//}
