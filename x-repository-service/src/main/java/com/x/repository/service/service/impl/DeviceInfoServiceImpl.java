package com.x.repository.service.service.impl;

import com.x.repository.service.base.BaseServiceImpl;
import com.x.repository.service.entity.DeviceInfoEntity;
import com.x.repository.service.mapper.DeviceInfoMapper;
import com.x.repository.service.service.IDeviceInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 *  服务实现类
 *
 * @author whj
 */
@Slf4j
@Service
public class DeviceInfoServiceImpl extends BaseServiceImpl<DeviceInfoMapper, DeviceInfoEntity> implements IDeviceInfoService {
}
