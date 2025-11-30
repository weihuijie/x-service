package com.x.file.service.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 同步上传任务管理服务
 * 
 * @author whj
 */
@Service
public class SyncUploadTaskService {
    
    // 存储任务状态的内存映射
    // 在生产环境中，这应该使用Redis或其他持久化存储
    private final Map<String, Map<String, Object>> taskStore = new ConcurrentHashMap<>();
    
    /**
     * 保存任务状态
     *
     * @param taskId 任务ID
     * @param taskInfo 任务信息
     */
    public void saveTask(String taskId, Map<String, Object> taskInfo) {
        taskStore.put(taskId, taskInfo);
    }
    
    /**
     * 获取任务状态
     *
     * @param taskId 任务ID
     * @return 任务信息
     */
    public Map<String, Object> getTask(String taskId) {
        return taskStore.get(taskId);
    }
    
    /**
     * 更新任务状态
     *
     * @param taskId 任务ID
     * @param updates 更新信息
     */
    public void updateTask(String taskId, Map<String, Object> updates) {
        Map<String, Object> taskInfo = taskStore.get(taskId);
        if (taskInfo != null) {
            taskInfo.putAll(updates);
            taskStore.put(taskId, taskInfo);
        }
    }
    
    /**
     * 删除任务信息
     *
     * @param taskId 任务ID
     */
    public void removeTask(String taskId) {
        taskStore.remove(taskId);
    }
}