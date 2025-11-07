package com.x.repository.service.service.impl;

import com.x.repository.service.base.BaseServiceImpl;
import com.x.repository.service.entity.AlertMsgEntity;
import com.x.repository.service.mapper.AlertMsgMapper;
import com.x.repository.service.service.IAlertMsgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 *  服务实现类
 *
 */
@Slf4j
@Service
public class AlertMsgServiceImpl extends BaseServiceImpl<AlertMsgMapper, AlertMsgEntity> implements IAlertMsgService {
}
