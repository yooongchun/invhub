package zoz.cool.apihub.interceptor;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import zoz.cool.apihub.enums.HttpCode;
import zoz.cool.apihub.exception.ApiException;
import zoz.cool.apihub.vo.ApiResponse;

/**
 * 全局异常处理类
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    // 未登录异常
    @ResponseBody
    @ExceptionHandler(value = NotLoginException.class)
    public ApiResponse<String> handlerNotLoginException(NotLoginException e) {
        log.error("未登录异常", e);
        return ApiResponse.unauthorized(e.getMessage());
    }

    // 未授权异常(角色)
    @ResponseBody
    @ExceptionHandler(value = NotRoleException.class)
    public ApiResponse<String> handlerNotRoleException(NotRoleException e) {
        log.error("未授权异常", e);
        return ApiResponse.forbidden(e.getMessage());
    }

    // 未授权异常(权限)
    @ResponseBody
    @ExceptionHandler(value = NotPermissionException.class)
    public ApiResponse<String> handlerNotPermissionException(NotPermissionException e) {
        log.error("未授权异常", e);
        return ApiResponse.forbidden(e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = ApiException.class)
    public ApiResponse<String> handleApiException(ApiException e) {
        log.error("Api请求异常", e);
        return e.getErrorCode() != null ? ApiResponse.failed(e.getErrorCode(), e.getMessage()) : ApiResponse.failed(e.getMessage());
    }

    // 捕捉其他所有异常
    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public ApiResponse<String> handle(Exception e) {
        log.error("未知异常", e);
        return ApiResponse.failed(HttpCode.INTERNAL_ERROR, e.getMessage());
    }

    // 请求参数校验异常
    @ResponseBody
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ApiResponse<String> handleValidException(MethodArgumentNotValidException e) {
        log.error("请求参数校验异常", e);
        return ApiResponse.validateFailed(e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = MaxUploadSizeExceededException.class)
    public ApiResponse<String> handleMaxUploadSizeExceededException(BindException e) {
        log.error("文件大小超出限制", e);
        return ApiResponse.failed("文件大小超出限制");
    }
}
