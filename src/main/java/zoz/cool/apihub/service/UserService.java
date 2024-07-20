package zoz.cool.apihub.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zoz.cool.apihub.dao.domain.ApihubLoginLog;
import zoz.cool.apihub.dao.domain.ApihubTransactionRecord;
import zoz.cool.apihub.dao.domain.ApihubUser;
import zoz.cool.apihub.dao.service.ApihubLoginLogService;
import zoz.cool.apihub.dao.service.ApihubTransactionRecordService;
import zoz.cool.apihub.dao.service.ApihubUserService;
import zoz.cool.apihub.enums.HttpCode;
import zoz.cool.apihub.enums.ProductNameEnum;
import zoz.cool.apihub.enums.TransactionStatusEnum;
import zoz.cool.apihub.exception.ApiException;
import zoz.cool.apihub.utils.RequestUtil;
import zoz.cool.apihub.utils.ToolKit;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Slf4j
public class UserService {
    @Resource
    private ApihubLoginLogService apihubLoginLogService;
    @Resource
    private ApihubUserService apihubUserService;
    @Resource
    private ApihubTransactionRecordService apihubTransactionRecordService;

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

    public boolean isAdmin() {
        return getLoginUser().getAdmin() == 1;
    }

    public void checkAdmin() {
        if (!isAdmin()) {
            throw new ApiException(HttpCode.FORBIDDEN, "需管理员身份");
        }
    }

    @Transactional
    public void deduceBalance(ApihubUser user, BigDecimal amount, ProductNameEnum transactionType, String msg) {
        if (user.getBalance().compareTo(amount) < 0) {
            throw new ApiException(HttpCode.BUSINESS_FAILED, "余额不足");
        }
        user.setBalance(user.getBalance().subtract(amount));
        apihubUserService.updateById(user);
        // 创建交易记录
        ApihubTransactionRecord record = new ApihubTransactionRecord();
        record.setUserId(user.getUid());
        record.setTransactionId(ToolKit.getUUID());
        record.setTransactionAmount(amount);
        record.setRemark(msg);
        record.setTransactionStatus(TransactionStatusEnum.SUCCEED.name());
        record.setTransactionTime(LocalDateTime.now());
        record.setTransactionType(transactionType.name());
        apihubTransactionRecordService.save(record);
    }
}
