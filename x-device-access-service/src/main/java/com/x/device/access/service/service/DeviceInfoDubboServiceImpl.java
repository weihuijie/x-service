package com.x.device.access.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.x.common.base.Query;
import com.x.common.base.R;
import com.x.dubbo.api.device.IDeviceInfoDubboService;
import com.x.dubbo.api.device.IDevicePointInfoDubboService;
import com.x.repository.service.entity.DeviceInfoEntity;
import com.x.repository.service.entity.DevicePointInfoEntity;
import com.x.repository.service.service.IDeviceInfoService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 *  服务实现类
 *
 * @author whj
 */
@Slf4j
@RestController
@RequestMapping("/device")
@DubboService(version = "1.0.0")
public class DeviceInfoDubboServiceImpl implements IDeviceInfoDubboService {

    @DubboReference(version = "1.0.0")
    private IDevicePointInfoDubboService devicePointInfoDubboService;

    @Autowired
    private IDeviceInfoService deviceInfoService;

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
    public R<List<DeviceInfoEntity>> list(@RequestBody DeviceInfoEntity device) {
        QueryWrapper<DeviceInfoEntity> condition = new QueryWrapper<>(device);
        List<DeviceInfoEntity> list = deviceInfoService.list(condition);
        return R.data(list);
    }

    /**
     * 详情
     */
    @GetMapping("/detail")
    public R<DeviceInfoEntity> detail(@RequestParam(name = "id") Long id) {
        log.info("id:{}", id);
        DeviceInfoEntity detail = deviceInfoService.getById(id);
        return R.data(detail);
    }

    /**
     * 新增或修改
     */
    @PostMapping("/submit")
    public R<Object> submit(@Valid @RequestBody DeviceInfoEntity device) {
        boolean updateResult = deviceInfoService.saveOrUpdate(device) ;
        return R.status(updateResult);
    }

    /**
     * 逻辑删除
     */
    @GetMapping("/remove")
    public R<Object> remove(@RequestParam(name = "id") Long id) {
        boolean updateResult = deviceInfoService.removeById(id) ;
        return R.status(updateResult);
    }

    @Override
    public R<List<DeviceInfoEntity>> listContainsPoint() {
        return R.data(listContainsPoint(null,null));
    }

    @Override
    public R<DeviceInfoEntity> infoContainsPoint(@RequestParam(name = "id") Long id) {
        List<DeviceInfoEntity> deviceInfoEntityList = listContainsPoint(id, null);
        if(ObjectUtils.isNotEmpty(deviceInfoEntityList)){
            if (deviceInfoEntityList.size() > 1){
                log.warn("设备信息列表中存在多个设备信息，请检查设备信息");
            }
            return R.data(deviceInfoEntityList.get(0));
        }
        return R.data(null);
    }

    @Override
    public R<DeviceInfoEntity> infoContainsPoint(String deviceCode) {
        List<DeviceInfoEntity> deviceInfoEntityList = listContainsPoint(null, deviceCode);
        if(ObjectUtils.isNotEmpty(deviceInfoEntityList)){
            if (deviceInfoEntityList.size() > 1){
                log.warn("设备信息列表中存在多个设备编码，请检查设备信息");
            }
            return R.data(deviceInfoEntityList.get(0));
        }
        return R.data(null);
    }

    private List<DeviceInfoEntity> listContainsPoint(Long id,String deviceCode){
        LambdaQueryWrapper<DeviceInfoEntity> qw = new LambdaQueryWrapper<>();
        qw.eq(ObjectUtils.isNotEmpty(id),DeviceInfoEntity::getId,id);
        qw.eq(ObjectUtils.isNotEmpty(deviceCode),DeviceInfoEntity::getDeviceCode,deviceCode);

        List<DeviceInfoEntity> deviceList = deviceInfoService.list(qw);

        for (DeviceInfoEntity deviceInfoEntity : deviceList) {
            DevicePointInfoEntity devicePointInfoEntity = new DevicePointInfoEntity();
            devicePointInfoEntity.setDeviceId(deviceInfoEntity.getId());
            List<DevicePointInfoEntity> pointList = devicePointInfoDubboService.list(devicePointInfoEntity).getData();
            deviceInfoEntity.setPointList(pointList);
        }
        return deviceList.stream().filter(e -> ObjectUtils.isNotEmpty(e.getPointList())).collect(Collectors.toList());
    }

}
