package zoz.cool.apihub.vo;

import lombok.Data;
import zoz.cool.apihub.enums.HttpCode;

@Data
public class ApiResponse<T> {
    private int status; // 业务状态码 0 成功 其他失败
    private int code; // http请求状态码
    private String message; // 提示信息
    private T data; // 数据封装

    public ApiResponse(int status, int code, String message, T data) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.data = data;
    }


    /**
     * 成功返回结果
     *
     * @param data 获取的数据
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(HttpCode.SUCCESS.getCode(), HttpCode.BUSINESS_SUCCESS.getCode(), HttpCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功返回结果
     *
     * @param data    获取的数据
     * @param message 提示信息
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(HttpCode.SUCCESS.getCode(), HttpCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 失败返回结果
     *
     * @param errorCode 错误码
     */
    public static <T> ApiResponse<T> failed(HttpCode errorCode) {
        return new ApiResponse<>(errorCode.getCode(), HttpCode.BUSINESS_FAILED.getCode(), errorCode.getMessage(), null);
    }

    /**
     * 失败返回结果
     *
     * @param errorCode 错误码
     * @param message   错误信息
     */
    public static <T> ApiResponse<T> failed(HttpCode errorCode, String message) {
        return new ApiResponse<>(errorCode.getCode(), HttpCode.BUSINESS_FAILED.getCode(), message, null);
    }

    /**
     * 失败返回结果
     *
     * @param message 提示信息
     */
    public static <T> ApiResponse<T> failed(String message) {
        return new ApiResponse<>(HttpCode.SUCCESS.getCode(), HttpCode.BUSINESS_FAILED.getCode(), message, null);
    }

    public static <T> ApiResponse<T> businessFailed(HttpCode businessCode, String message) {
        return new ApiResponse<>(HttpCode.SUCCESS.getCode(), businessCode.getCode(), message, null);
    }

    /**
     * 失败返回结果
     */
    public static <T> ApiResponse<T> failed() {
        return failed(HttpCode.FAILED);
    }

    /**
     * 参数验证失败返回结果
     */
    public static <T> ApiResponse<T> validateFailed() {
        return failed(HttpCode.VALIDATE_FAILED);
    }

    /**
     * 参数验证失败返回结果
     *
     * @param message 提示信息
     */
    public static <T> ApiResponse<T> validateFailed(String message) {
        return new ApiResponse<>(HttpCode.VALIDATE_FAILED.getCode(), HttpCode.BUSINESS_FAILED.getCode(), message, null);
    }

    /**
     * 未登录返回结果
     */
    public static <T> ApiResponse<T> unauthorized(T data) {
        return new ApiResponse<>(HttpCode.UNAUTHORIZED.getCode(), HttpCode.BUSINESS_FAILED.getCode(), HttpCode.UNAUTHORIZED.getMessage(), data);
    }

    /**
     * 未授权返回结果
     */
    public static <T> ApiResponse<T> forbidden(T data) {
        return new ApiResponse<>(HttpCode.FORBIDDEN.getCode(), HttpCode.BUSINESS_FAILED.getCode(), HttpCode.FORBIDDEN.getMessage(), data);
    }
}