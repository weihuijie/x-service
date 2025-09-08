package com.x.common.component;

import com.xxl.job.core.context.XxlJobHelper;
import org.springframework.stereotype.Component;

/**
 * XXL-JOB组件工具类
 * 提供XXL-JOB任务调度操作常用方法的封装
 */
@Component
public class XxlJobComponentUtil {
    
    /**
     * 获取任务参数
     * @return 任务参数
     */
    public String getJobParam() {
        return XxlJobHelper.getJobParam();
    }
    
    /**
     * 记录任务日志
     * @param logContent 日志内容
     */
    public void log(String logContent) {
        XxlJobHelper.log(logContent);
    }
    
    /**
     * 设置任务处理成功
     */
    public void handleSuccess() {
        XxlJobHelper.handleSuccess();
    }
    
    /**
     * 设置任务处理失败
     * @param errorMsg 错误信息
     */
    public void handleFail(String errorMsg) {
        XxlJobHelper.handleFail(errorMsg);
    }
    
    /**
     * 设置任务处理失败
     */
    public void handleFail() {
        XxlJobHelper.handleFail();
    }
    
    /**
     * 获取当前任务ID
     * @return 任务ID
     */
    public long getJobId() {
        return XxlJobHelper.getJobId();
    }
    
    /**
     * 获取分片索引
     * @return 分片索引
     */
    public int getShardIndex() {
        return XxlJobHelper.getShardIndex();
    }
    
    /**
     * 获取分片总数
     * @return 分片总数
     */
    public int getShardTotal() {
        return XxlJobHelper.getShardTotal();
    }
    
    /**
     * 获取环境变量
     * @param key 键
     * @return 值
     */
    public String getEnv(String key) {
        // XXL-JOB中没有直接获取环境变量的方法，这里返回null
        return System.getenv(key);
    }
}