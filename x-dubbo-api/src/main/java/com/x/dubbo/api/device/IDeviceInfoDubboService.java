package com.x.dubbo.api.device;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.x.common.base.Query;
import com.x.common.base.R;
import com.x.repository.service.entity.DeviceInfoEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 *  服务接口
 *
 *  @author whj
 */
public interface IDeviceInfoDubboService {

    /**
     * 分页
     */
    R<IPage<DeviceInfoEntity>> list(DeviceInfoEntity device, Query query);


    /**
     * 不分页
     */
    R<List<DeviceInfoEntity>> list(@RequestBody DeviceInfoEntity device);

    /**
     * 详情
     */
    R<DeviceInfoEntity> detail(@RequestParam(name = "id") Long id);

    /**
     * 新增或修改
     */
    R<Object> submit(@Valid @RequestBody DeviceInfoEntity device);

    /**
     * 逻辑删除
     */
    R<Object> remove(@RequestParam(name = "id") Long id);

    /**
     * 获取设备信息，包含点位
     */
    R<List<DeviceInfoEntity>> listContainsPoint(@RequestParam(name = "id",required = false) Long id);
}
