package com.x.data.collection.service.adapter.validation;

import lombok.Data;

/**
 * 数据校验结果
 *
 * @author whj
 */
@Data
public class ValidationResult {
    /**
     * 是否校验通过
     */
    private boolean valid;
    
    /**
     * 错误信息
     */
    private String errorMessage;
}