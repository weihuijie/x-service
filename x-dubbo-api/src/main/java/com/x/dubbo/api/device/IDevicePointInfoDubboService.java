package com.x.dubbo.api.device;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.x.common.base.Query;
import com.x.common.base.R;
import com.x.repository.service.entity.DevicePointInfoEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface IDevicePointInfoDubboService {

    /**
     * 分页
     */
    R<IPage<DevicePointInfoEntity>> list(DevicePointInfoEntity device, Query query);


    /**
     * 不分页
     */
    R<List<DevicePointInfoEntity>> list(@RequestBody DevicePointInfoEntity device);

    /**
     * 详情
     */
    R<DevicePointInfoEntity> detail(@RequestParam(name = "id") Long id);

    /**
     * 新增或修改
     */
    R submit(@Valid @RequestBody DevicePointInfoEntity device);

    /**
     * 逻辑删除
     */
    R remove(@RequestParam(name = "id") Long id);
}
