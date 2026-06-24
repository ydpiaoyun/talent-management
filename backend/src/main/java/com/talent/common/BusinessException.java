package com.talent.common;

/**
 * 业务异常
 * <p>
 * 用于在业务逻辑中抛出可预期的异常，由全局异常处理器统一捕获并返回友好提示。
 * </p>
 *
 * @author talent-hr
 */
public class BusinessException extends RuntimeException {

    private final int code;

    /**
     * 构造业务异常（默认错误码 400）
     *
     * @param message 异常提示信息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    /**
     * 构造业务异常
     *
     * @param code    HTTP 状态码
     * @param message 异常提示信息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 获取错误码
     *
     * @return 错误码
     */
    public int getCode() {
        return code;
    }
}
