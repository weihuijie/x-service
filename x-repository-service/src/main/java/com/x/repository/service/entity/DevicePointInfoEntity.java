package com.x.repository.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.x.repository.service.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * @author whj
 */
@Data
@TableName("device_point_info")
@EqualsAndHashCode(callSuper = true)
public class DevicePointInfoEntity extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long deviceId;

    private String deviceCode;

    private String pointAddr;

    private String pointName;

    private Integer pointType;

    @TableField(exist = false)
    private Object pointValue;
}
