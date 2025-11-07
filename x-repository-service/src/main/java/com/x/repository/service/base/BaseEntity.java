package com.x.repository.service.base;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础公共类
 * @author whj
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseEntity implements Serializable {

    /**
     *  主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     *  创建时间
     */
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     *  创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private String createUser;

    /**
     *  更新时间
     */
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    /**
     *  更新人
     */
    @TableField(fill = FieldFill.UPDATE)
    private String updateUser;

    /**
     *  状态
     */
    private Integer status;
    /**
     * 是否已删除
     */
    @TableLogic
    private Integer isDeleted;


    /**
     * 当前页
     */
    @TableField(exist = false)
    private Integer pageNo = 1;

    /**
     * 每页的数量
     */
    @TableField(exist = false)
    private Integer pageSize = 10;
}
