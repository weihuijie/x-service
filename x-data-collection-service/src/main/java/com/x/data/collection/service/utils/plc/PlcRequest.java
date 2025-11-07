package com.x.data.collection.service.utils.plc;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.x.common.utils.http.HttpEnhancedUtils;
import com.x.data.collection.service.utils.plc.read.PlcReadReq;
import com.x.data.collection.service.utils.plc.read.PlcReadRes;
import com.x.data.collection.service.utils.plc.write.PlcWriteReq;
import com.x.data.collection.service.utils.plc.write.PlcWriteRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class PlcRequest {

    @Value("${plc-os.ip:127.0.0.1}")
    private String cuServiceIp;
    @Value("${plc-os.bizLog:false}")
    private Boolean bizLog;
    /**
     *  获取PLC数据
     */
    public PlcReadRes getPlcData(List<PlcReadReq> request,String uid){
        if (bizLog) {
            log.info("获取PLC数据异步请求参数 uid{}, req:{}", uid, JSON.toJSONString(request));
        }
        CompletableFuture<Object> future = HttpEnhancedUtils.post(cuServiceIp+"/PLC/Read/"+uid)
                .jsonBody(JSONObject.toJSONString(request))
                .retryTimes(3)
                .sendAsync();

        future.whenComplete((response, ex) -> {
            if (ex == null) {
                if (bizLog) {
                    log.info("获取PLC数据响应结果：{}", response);
                }
            } else {
                if (ex instanceof HttpEnhancedUtils.HttpRetryException retryEx) {
                    if (bizLog) {
                        log.error("获取PLC数据异步请求失败：{}，URL：{}", retryEx.getMessage(), retryEx.getUrl());
                    }
                } else {
                    if (bizLog){
                        log.error("获取PLC数据异步请求异常", ex);
                    }
                }
            }
        });
        return JSONObject.parseObject(JSONObject.toJSONString(future.join()),PlcReadRes.class);
    }

    /**
     *  设置PLC数据
     */
    public PlcWriteRes setPlcData(List<PlcWriteReq> request,String uid){
        if (bizLog) {
            log.info("设置PLC数据异步请求参数 uid{}, req:{}", uid, JSON.toJSONString(request));
        }
        CompletableFuture<Object> future = HttpEnhancedUtils.post(cuServiceIp+"/PLC/Write/"+uid)
                .jsonBody(JSONObject.toJSONString(request))
                .retryTimes(3)
                .sendAsync();

        future.whenComplete((response, ex) -> {
            if (ex == null) {
                if (bizLog) {
                    log.info("设置PLC数据响应结果：{}", response);
                }
            } else {
                if (ex instanceof HttpEnhancedUtils.HttpRetryException retryEx) {
                    if (bizLog) {
                        log.error("设置PLC数据异步请求失败：{}，URL：{}", retryEx.getMessage(), retryEx.getUrl());
                    }
                } else {
                    if (bizLog){
                        log.error("设置PLC数据异步请求异常", ex);
                    }
                }
            }
        });
        return JSONObject.parseObject(JSONObject.toJSONString(future.join()),PlcWriteRes.class);
    }

    /**
     *  获取PLC状态
     */
    public PlcStatus getPlcStatus(){
        if (bizLog) {
            log.info("获取PLC状态异步请求");
        }
        CompletableFuture<Object> future = HttpEnhancedUtils.get(cuServiceIp+"/PLC/Status/ReadService")
                .retryTimes(3)
                .sendAsync();

        future.whenComplete((response, ex) -> {
            if (ex == null) {
                if (bizLog) {
                    log.info("获取PLC状态响应结果：{}", response);
                }
            } else {
                if (ex instanceof HttpEnhancedUtils.HttpRetryException retryEx) {
                    if (bizLog) {
                        log.error("获取PLC状态异步请求失败：{}，URL：{}", retryEx.getMessage(), retryEx.getUrl());
                    }
                } else {
                    if (bizLog){
                        log.error("获取PLC状态异步请求异常", ex);
                    }
                }
            }
        });
        return JSONObject.parseObject(JSONObject.toJSONString(future.join()),PlcStatus.class);
    }
}
