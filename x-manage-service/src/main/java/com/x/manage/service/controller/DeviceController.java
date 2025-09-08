package com.x.manage.service.controller;

import com.x.manage.service.entity.Device;
import com.x.manage.service.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/device")
public class DeviceController {
    
    @Autowired
    private DeviceService deviceService;
    
    /**
     * 获取设备列表
     */
    @GetMapping("/list")
    public List<Device> getDeviceList() {
        return deviceService.getAllDevices();
    }
    
    /**
     * 根据ID获取设备详情
     */
    @GetMapping("/detail/{id}")
    public Device getDeviceDetail(@PathVariable Long id) {
        return deviceService.getDeviceById(id);
    }
    
    /**
     * 添加设备
     */
    @PostMapping("/add")
    public boolean addDevice(@RequestBody Device device) {
        return deviceService.addDevice(device);
    }
    
    /**
     * 更新设备信息
     */
    @PutMapping("/update")
    public boolean updateDevice(@RequestBody Device device) {
        return deviceService.updateDevice(device);
    }
    
    /**
     * 删除设备
     */
    @DeleteMapping("/delete/{id}")
    public boolean deleteDevice(@PathVariable Long id) {
        return deviceService.deleteDevice(id);
    }
}