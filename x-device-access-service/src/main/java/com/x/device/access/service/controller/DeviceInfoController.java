package com.x.device.access.service.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.x.common.base.Query;
import com.x.common.base.R;
import com.x.repository.service.entity.DeviceInfoEntity;
import com.x.repository.service.service.IDeviceInfoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/device")
public class DeviceInfoController {
    
    private final IDeviceInfoService deviceInfoService;

    public DeviceInfoController(IDeviceInfoService deviceInfoService) {
        this.deviceInfoService = deviceInfoService;
    }

    /**
     * 分页
     */
    @ResponseBody
    @PostMapping("/list")
    public R<IPage<DeviceInfoEntity>> list(DeviceInfoEntity device, Query query) {
        QueryWrapper<DeviceInfoEntity> condition = new QueryWrapper<>(device);
        Page<DeviceInfoEntity> page = new Page<>(query.getPageNo(),query.getPageSize());
        IPage<DeviceInfoEntity> pages = deviceInfoService.page(page, condition);
        return R.data(pages);
    }


    /**
     * 不分页
     */
    @ResponseBody
    @PostMapping("/list/all")
    public R list(@RequestBody DeviceInfoEntity device) {
        QueryWrapper<DeviceInfoEntity> condition = new QueryWrapper<>(device);
        List<DeviceInfoEntity> list = deviceInfoService.list(condition);
        return R.data(list);
    }

    /**
     * 详情
     */
    @GetMapping("/detail")
    public R<DeviceInfoEntity> detail(@RequestParam(name = "id") Long id) {
        DeviceInfoEntity detail = deviceInfoService.getById(id);
        return R.data(detail);
    }

    /**
     * 新增或修改
     */
    @PostMapping("/submit")
    public R submit(@Valid @RequestBody DeviceInfoEntity device) {
        boolean updateResult = deviceInfoService.saveOrUpdate(device) ;
        return R.status(updateResult);
    }

    /**
     * 逻辑删除
     */
    @GetMapping("/remove")
    public R remove(@RequestParam(name = "id") Long id) {
        boolean updateResult = deviceInfoService.removeById(id) ;
        return R.status(updateResult);
    }
}