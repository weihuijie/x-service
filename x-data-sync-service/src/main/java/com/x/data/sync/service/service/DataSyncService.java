package com.x.data.sync.service.service;

import org.springframework.stereotype.Service;

/**
 * 数据同步服务
 *
 * @author whj
 */
@Service
public class DataSyncService {
    
    /**
     * 从IoTDB同步数据到HDFS
     */
    public void syncFromIoTDBToHDFS() {
        System.out.println("Syncing data from IoTDB to HDFS");
    }
    
    /**
     * 从MySQL同步数据到HDFS
     */
    public void syncFromMySQLToHDFS() {
        System.out.println("Syncing data from MySQL to HDFS");
    }
    
    /**
     * 定时同步任务
     */
    public void scheduleSyncTask() {
        System.out.println("Scheduling sync task");
    }
}