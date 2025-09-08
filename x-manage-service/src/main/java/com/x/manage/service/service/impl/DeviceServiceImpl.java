package com.x.manage.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.x.manage.service.entity.Device;
import com.x.manage.service.mapper.DeviceMapper;
import com.x.manage.service.service.DeviceService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device> implements DeviceService {
    
    @Override
    public List<Device> getAllDevices() {
        return list();
    }
    
    @Override
    public Device getDeviceById(Long id) {
        return getById(id);
    }
    
    @Override
    public boolean addDevice(Device device) {
        return save(device);
    }
    
    @Override
    public boolean updateDevice(Device device) {
        return updateById(device);
    }
    
    @Override
    public boolean deleteDevice(Long id) {
        return removeById(id);
    }
}