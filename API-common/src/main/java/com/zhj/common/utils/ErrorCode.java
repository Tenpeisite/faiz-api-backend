package com.zhj.common.utils;

/**
 * 错误码
 *
 * @author zhj
 */
public enum ErrorCode {

    SUCCESS(0, "ok"),
    PARAMS_ERROR(40000, "请求参数错误"),
    PROHIBITED(40001, "账号已封禁"),
    NOT_LOGIN_ERROR(40100, "未登录"),
    NO_AUTH_ERROR(40101, "无权限"),
    NOT_FOUND_ERROR(40400, "请求数据不存在"),
    FORBIDDEN_ERROR(40300, "禁止访问"),
    SYSTEM_ERROR(50000, "系统内部异常"),
    OPERATION_ERROR(50001, "操作失败"),
    INTERFACE_ERROR(50002, "接口调用异常"),
    INTERFACE_USE_ERROR(50003, "接口调用请走网关"),
    INTERFACE_USE_FREQUENTLY(50004, "接口正被频繁调用，请稍后再试")
    ;

    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
