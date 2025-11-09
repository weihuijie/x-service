package com.x.repository.service.service.impl;

import com.x.repository.service.base.BaseServiceImpl;
import com.x.repository.service.entity.DevicePointInfoEntity;
import com.x.repository.service.mapper.DevicePointInfoMapper;
import com.x.repository.service.service.IDevicePointInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 *  服务实现类
 *
 */
@Slf4j
@Service
public class DevicePointInfoServiceImpl extends BaseServiceImpl<DevicePointInfoMapper, DevicePointInfoEntity> implements IDevicePointInfoService {
}
