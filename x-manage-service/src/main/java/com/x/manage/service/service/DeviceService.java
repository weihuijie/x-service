package com.x.manage.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.x.manage.service.entity.Device;

import java.util.List;

public interface DeviceService extends IService<Device> {
    
    /**
     * 获取所有设备列表
     * @return 设备列表
     */
    List<Device> getAllDevices();
    
    /**
     * 根据ID获取设备详情
     * @param id 设备ID
     * @return 设备信息
     */
    Device getDeviceById(Long id);
    
    /**
     * 添加设备
     * @param device 设备信息
     * @return 是否添加成功
     */
    boolean addDevice(Device device);
    
    /**
     * 更新设备信息
     * @param device 设备信息
     * @return 是否更新成功
     */
    boolean updateDevice(Device device);
    
    /**
     * 删除设备
     * @param id 设备ID
     * @return 是否删除成功
     */
    boolean deleteDevice(Long id);
}