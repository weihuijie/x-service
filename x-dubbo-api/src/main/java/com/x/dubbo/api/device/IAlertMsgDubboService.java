package com.x.dubbo.api.device;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.x.common.base.Query;
import com.x.common.base.R;
import com.x.repository.service.entity.AlertMsgEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface IAlertMsgDubboService {

    /**
     * 分页
     */
    R<IPage<AlertMsgEntity>> list(AlertMsgEntity device, Query query);


    /**
     * 不分页
     */
    R<List<AlertMsgEntity>> list(@RequestBody AlertMsgEntity device);

    /**
     * 详情
     */
    R<AlertMsgEntity> detail(@RequestParam(name = "id") Long id);

    /**
     * 新增或修改
     */
    R submit(@Valid @RequestBody AlertMsgEntity device);

    /**
     * 逻辑删除
     */
    R remove(@RequestParam(name = "id") Long id);
}
