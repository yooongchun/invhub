package zoz.cool.apihub.service;

import cn.hutool.core.lang.Assert;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import zoz.cool.apihub.dao.domain.ApihubLoginLog;
import zoz.cool.apihub.dao.domain.ApihubUser;
import zoz.cool.apihub.dao.service.ApihubLoginLogService;
import zoz.cool.apihub.utils.RequestUtil;

import java.time.LocalDateTime;

@Service
@Slf4j
public class UserService {
    @Resource
    private ApihubLoginLogService apihubLoginLogService;

    @Async
    public void insertLoginLog(ApihubUser user) {
        ApihubLoginLog apihubLoginLog = new ApihubLoginLog();
        apihubLoginLog.setUserId(user.getUid());
        apihubLoginLog.setCreateTime(LocalDateTime.now());
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        Assert.notNull(attributes, "无法获取请求信息！");
        assert attributes != null;
        HttpServletRequest request = attributes.getRequest();
        apihubLoginLog.setIp(RequestUtil.getRequestIp(request));
        apihubLoginLogService.save(apihubLoginLog);
        log.info("记录用户登录日志：{}", apihubLoginLog);
    }
}
