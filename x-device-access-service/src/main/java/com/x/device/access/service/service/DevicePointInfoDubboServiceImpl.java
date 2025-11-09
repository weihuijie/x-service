package com.x.device.access.service.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.x.common.base.Query;
import com.x.common.base.R;
import com.x.dubbo.api.device.IDevicePointInfoDubboService;
import com.x.repository.service.entity.DevicePointInfoEntity;
import com.x.repository.service.service.IDevicePointInfoService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *  服务实现类
 *
 */
@Slf4j
@RestController
@RequestMapping("/device/point")
@DubboService(version = "1.0.0")
public class DevicePointInfoDubboServiceImpl implements IDevicePointInfoDubboService {

    private final IDevicePointInfoService devicePointInfoService;

    public DevicePointInfoDubboServiceImpl(IDevicePointInfoService devicePointInfoService) {
        this.devicePointInfoService = devicePointInfoService;
    }
    /**
     * 分页
     */
    @ResponseBody
    @PostMapping("/list")
    public R<IPage<DevicePointInfoEntity>> list(DevicePointInfoEntity device, Query query) {
        QueryWrapper<DevicePointInfoEntity> condition = new QueryWrapper<>(device);
        Page<DevicePointInfoEntity> page = new Page<>(query.getPageNo(),query.getPageSize());
        IPage<DevicePointInfoEntity> pages = devicePointInfoService.page(page, condition);
        return R.data(pages);
    }


    /**
     * 不分页
     */
    @ResponseBody
    @PostMapping("/list/all")
    public R<List<DevicePointInfoEntity>> list(@RequestBody DevicePointInfoEntity device) {
        QueryWrapper<DevicePointInfoEntity> condition = new QueryWrapper<>(device);
        List<DevicePointInfoEntity> list = devicePointInfoService.list(condition);
        return R.data(list);
    }

    /**
     * 详情
     */
    @GetMapping("/detail")
    public R<DevicePointInfoEntity> detail(@RequestParam(name = "id") Long id) {
        log.info("id:{}", id);
        DevicePointInfoEntity detail = devicePointInfoService.getById(id);
        return R.data(detail);
    }

    /**
     * 新增或修改
     */
    @PostMapping("/submit")
    public R submit(@Valid @RequestBody DevicePointInfoEntity device) {
        boolean updateResult = devicePointInfoService.saveOrUpdate(device) ;
        return R.status(updateResult);
    }

    /**
     * 逻辑删除
     */
    @GetMapping("/remove")
    public R remove(@RequestParam(name = "id") Long id) {
        boolean updateResult = devicePointInfoService.removeById(id) ;
        return R.status(updateResult);
    }
}
