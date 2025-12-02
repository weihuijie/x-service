package com.x.repository.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.x.repository.service.base.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.List;

/**
 * 设备表
 * @author whj
 */
@Data
@TableName("device_info")
@EqualsAndHashCode(callSuper = true)
public class DeviceInfoEntity extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "设备名称不能为空")
    private String deviceName;

    @NotBlank(message = "设备编码不能为空")
    private String deviceCode;

    private String dataCode;

    @TableField(exist = false)
    private Long timestamp;

    @TableField(exist = false)
    private List<DevicePointInfoEntity> pointList;
}
