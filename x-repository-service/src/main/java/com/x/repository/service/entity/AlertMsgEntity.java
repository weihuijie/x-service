package com.x.repository.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.x.repository.service.base.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 钢板表
 * @author whj
 */
@Builder
@Data
@TableName("alert_msg")
@EqualsAndHashCode(callSuper = true)
public class AlertMsgEntity extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "设备id不能为空")
    private Long deviceId;

    private Long pointId;

    @NotBlank(message = "点位地址不能为空")
    private String pointAddr;

    private String pointValue;

    private Long timestamp;
}
