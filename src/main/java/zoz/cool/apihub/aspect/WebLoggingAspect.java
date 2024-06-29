package zoz.cool.apihub.aspect;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import zoz.cool.apihub.utils.RequestUtil;
import zoz.cool.apihub.vo.WebLoggingVo;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一日志处理切面
 */
@Aspect
@Component
@Order(1)
@Slf4j
public class WebLoggingAspect {

    @Pointcut("execution(public * zoz.cool.apihub.controller.*.*(..))||execution(public * zoz.cool.apihub.*.controller.*.*(..))")
    public void webLogHandler() {
    }

    @Around("webLogHandler()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        // 获取当前请求对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert attributes != null;
        HttpServletRequest request = attributes.getRequest();
        WebLoggingVo webLogVo = new WebLoggingVo();
        Object result = joinPoint.proceed();
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        if (method.isAnnotationPresent(Operation.class)) {
            Operation log = method.getAnnotation(Operation.class);
            webLogVo.setDescription(log.description());
        }
        long endTime = System.currentTimeMillis();
        String urlStr = request.getRequestURL().toString();
        webLogVo.setBasePath(StrUtil.removeSuffix(urlStr, URLUtil.url(urlStr).getPath()));
        webLogVo.setUsername(request.getRemoteUser());
        webLogVo.setIp(RequestUtil.getRequestIp(request));
        webLogVo.setMethod(request.getMethod());
        webLogVo.setParameter(getParameter(method, joinPoint.getArgs()));
        webLogVo.setResult(result);
        webLogVo.setSpendTime((int) (endTime - startTime));
        webLogVo.setStartTime(startTime);
        webLogVo.setUri(request.getRequestURI());
        webLogVo.setUrl(request.getRequestURL().toString());
        log.info(JSONUtil.parse(webLogVo).toString());
        return result;
    }

    /**
     * 根据方法和传入的参数获取请求参数
     */
    private Object getParameter(Method method, Object[] args) {
        List<Object> argList = new ArrayList<>();
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            // 将RequestBody注解修饰的参数作为请求参数
            RequestBody requestBody = parameters[i].getAnnotation(RequestBody.class);
            if (requestBody != null) {
                argList.add(args[i]);
            }
            // 将RequestParam注解修饰的参数作为请求参数
            RequestParam requestParam = parameters[i].getAnnotation(RequestParam.class);
            if (requestParam != null) {
                Map<String, Object> map = new HashMap<>();
                String key = parameters[i].getName();
                if (!StrUtil.isEmpty(requestParam.value())) {
                    key = requestParam.value();
                }
                if (args[i] != null) {
                    map.put(key, args[i]);
                    argList.add(map);
                }
            }
        }
        if (argList.isEmpty()) {
            return null;
        } else if (argList.size() == 1) {
            return argList.getFirst();
        } else {
            return argList;
        }
    }
}
