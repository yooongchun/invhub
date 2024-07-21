package zoz.cool.apihub.enums;

import lombok.Getter;

/**
 * API返回码接口 Created by zhayongchun on 2023/11/16.
 */

@Getter
public enum HttpCode {
    SUCCESS(200, "操作成功"),
    FAILED(200, "操作失败"),
    INTERNAL_ERROR(500, "服务器内部错误"),
    VALIDATE_FAILED(400, "参数检验失败"),
    UNAUTHORIZED(401, "暂未登录或token已经过期"),
    FORBIDDEN(403, "没有相关权限"),
    NOT_FOUND(404, "找不到资源"),
    BUSINESS_SUCCESS(0, "成功"),
    BUSINESS_FAILED(2001, "失败"),
    BUSINESS_ERROR(2002, "错误"),
    BUSINESS_EXCEPTION(2003, "异常"),
    BUSINESS_TIMEOUT(2004, "超时"),
    BUSINESS_RETRY(2005, "重试"),
    BUSINESS_EXISTS(2006, "已存在"),
    BUSINESS_UNKNOWN(2009, "未知");

    private final int code;
    private final String message;

    HttpCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static HttpCode getHttpCode(int code) {
        for (HttpCode httpCode : HttpCode.values()) {
            if (httpCode.getCode() == code) {
                return httpCode;
            }
        }
        return null;
    }
}
