package zoz.cool.apihub.interceptor;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import zoz.cool.apihub.vo.ApiResponse;

@ControllerAdvice(basePackages = "zoz.cool.apihub.controller")
@Slf4j
public class GlobalResponseAdvice implements ResponseBodyAdvice {

    @Override
    public boolean supports(MethodParameter methodParameter, Class aClass) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter methodParameter, MediaType mediaType, Class aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        if (body instanceof InputStreamResource) {
            return body;
        }
        if (body instanceof ApiResponse<?>) {
            return body;
        }
        if (body instanceof String) {
            return JSONUtil.toJsonStr(ApiResponse.success(body));
        }
        return ApiResponse.success(body);
    }
}
