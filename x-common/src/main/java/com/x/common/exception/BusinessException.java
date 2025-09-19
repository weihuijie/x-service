package com.x.common.exception;

import com.x.common.base.IResultCode;

/**
 * 业务异常类 - 用于封装业务逻辑中的错误
 * 与ResultCode结合使用，便于统一处理业务异常
 */
public class BusinessException extends RuntimeException {
    
    private final int code;
    
    /**
     * 使用自定义状态码和消息创建业务异常
     * @param code 状态码
     * @param message 错误消息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
    
    /**
     * 使用IResultCode创建业务异常
     * @param resultCode 结果码接口实现
     */
    public BusinessException(IResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }
    
    /**
     * 获取异常状态码
     * @return 状态码
     */
    public int getCode() {
        return code;
    }
    
    /**
     * 创建业务异常的便捷方法
     * @param code 状态码
     * @param message 错误消息
     * @return BusinessException实例
     */
    public static BusinessException of(int code, String message) {
        return new BusinessException(code, message);
    }
    
    /**
     * 创建业务异常的便捷方法
     * @param resultCode 结果码接口实现
     * @return BusinessException实例
     */
    public static BusinessException of(IResultCode resultCode) {
        return new BusinessException(resultCode);
    }
}