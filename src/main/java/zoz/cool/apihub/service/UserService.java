package zoz.cool.apihub.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import zoz.cool.apihub.dao.domain.ApihubLoginLog;
import zoz.cool.apihub.dao.domain.ApihubUser;
import zoz.cool.apihub.dao.service.ApihubLoginLogService;
import zoz.cool.apihub.dao.service.ApihubUserService;
import zoz.cool.apihub.exception.ApiException;
import zoz.cool.apihub.utils.RequestUtil;

import java.time.LocalDateTime;

@Service
@Slf4j
public class UserService {
    @Resource
    private ApihubLoginLogService apihubLoginLogService;
    @Resource
    private ApihubUserService apihubUserService;

    @Async
    public void insertLoginLog(ApihubUser user, HttpServletRequest request) {
        ApihubLoginLog apihubLoginLog = new ApihubLoginLog();
        apihubLoginLog.setUserId(user.getUid());
        apihubLoginLog.setCreateTime(LocalDateTime.now());
        apihubLoginLog.setIp(RequestUtil.getRequestIp(request));
        String userAgent = request.getHeader("User-Agent");
        apihubLoginLog.setUserAgent(userAgent);
        apihubLoginLogService.save(apihubLoginLog);
        log.info("记录用户登录日志：{}", apihubLoginLog);
    }

    public ApihubUser getLoginUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        ApihubUser user = apihubUserService.getUserByUid(userId);
        if (user == null) {
            throw new ApiException("用户不存在");
        }
        return user;
    }
}
