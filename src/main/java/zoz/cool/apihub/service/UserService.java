package zoz.cool.apihub.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zoz.cool.apihub.dao.domain.ApihubLoginLog;
import zoz.cool.apihub.dao.domain.ApihubUser;
import zoz.cool.apihub.dao.service.ApihubLoginLogService;
import zoz.cool.apihub.dao.service.ApihubUserService;
import zoz.cool.apihub.enums.HttpCode;
import zoz.cool.apihub.exception.ApiException;
import zoz.cool.apihub.utils.RequestUtil;

import java.math.BigDecimal;
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

    @Transactional
    public void deduceBalance(ApihubUser user, BigDecimal amount) {
        if (user.getBalance().compareTo(amount) < 0) {
            throw new ApiException(HttpCode.BUSINESS_FAILED, "余额不足");
        }
        user.setBalance(user.getBalance().subtract(amount));
        apihubUserService.updateById(user);
    }
}
