package com.x.alert.notice.service.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.x.common.base.Query;
import com.x.common.base.R;
import com.x.dubbo.api.device.IAlertMsgDubboService;
import com.x.repository.service.entity.AlertMsgEntity;
import com.x.repository.service.service.IAlertMsgService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *  服务实现类
 *  @author whj
 */
@Slf4j
@RestController
@RequestMapping("/device")
@DubboService(version = "1.0.0")
public class AlertMsgDubboServiceImpl implements IAlertMsgDubboService {

    private final IAlertMsgService alertMsgService;

    public AlertMsgDubboServiceImpl(IAlertMsgService alertMsgService) {
        this.alertMsgService = alertMsgService;
    }
    /**
     * 分页
     */
    @ResponseBody
    @PostMapping("/list")
    public R<IPage<AlertMsgEntity>> list(AlertMsgEntity device, Query query) {
        QueryWrapper<AlertMsgEntity> condition = new QueryWrapper<>(device);
        Page<AlertMsgEntity> page = new Page<>(query.getPageNo(),query.getPageSize());
        IPage<AlertMsgEntity> pages = alertMsgService.page(page, condition);
        return R.data(pages);
    }


    /**
     * 不分页
     */
    @ResponseBody
    @PostMapping("/list/all")
    public R<List<AlertMsgEntity>> list(@RequestBody AlertMsgEntity device) {
        QueryWrapper<AlertMsgEntity> condition = new QueryWrapper<>(device);
        List<AlertMsgEntity> list = alertMsgService.list(condition);
        return R.data(list);
    }

    /**
     * 详情
     */
    @GetMapping("/detail")
    public R<AlertMsgEntity> detail(@RequestParam(name = "id") Long id) {
        log.info("id:{}", id);
        AlertMsgEntity detail = alertMsgService.getById(id);
        return R.data(detail);
    }

    /**
     * 新增或修改
     */
    @PostMapping("/submit")
    public R<Object> submit(@RequestBody AlertMsgEntity device) {
        boolean updateResult = alertMsgService.saveOrUpdate(device) ;
        return R.status(updateResult);
    }

    /**
     * 逻辑删除
     */
    @GetMapping("/remove")
    public R<Object> remove(@RequestParam(name = "id") Long id) {
        boolean updateResult = alertMsgService.removeById(id) ;
        return R.status(updateResult);
    }
}
