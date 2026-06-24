package com.talent.common;

import lombok.Data;

/**
 * 统一响应封装
 * <p>
 * 所有接口返回统一格式：{ code, message, data }
 * </p>
 *
 * @param <T> 响应数据类型
 * @author talent-hr
 */
@Data
public class R<T> {

    /** 成功状态码 */
    public static final int SUCCESS_CODE = 200;

    /** 成功提示 */
    public static final String SUCCESS_MESSAGE = "操作成功";

    /** 服务器错误码 */
    public static final int SERVER_ERROR_CODE = 500;

    /** 服务器错误提示 */
    public static final String SERVER_ERROR_MESSAGE = "操作失败";

    private int code;
    private String message;
    private T data;

    private R(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功响应（带数据）
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功响应
     */
    public static <T> R<T> ok(T data) {
        return new R<>(SUCCESS_CODE, SUCCESS_MESSAGE, data);
    }

    /**
     * 成功响应（无数据）
     *
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> R<T> ok() {
        return new R<>(SUCCESS_CODE, SUCCESS_MESSAGE, null);
    }

    /**
     * 失败响应（默认 500 错误码）
     *
     * @param message 错误提示
     * @param <T>     数据类型
     * @return 失败响应
     */
    public static <T> R<T> fail(String message) {
        return new R<>(SERVER_ERROR_CODE, message, null);
    }

    /**
     * 失败响应（自定义错误码）
     *
     * @param code    错误码
     * @param message 错误提示
     * @param <T>     数据类型
     * @return 失败响应
     */
    public static <T> R<T> fail(int code, String message) {
        return new R<>(code, message, null);
    }
}
