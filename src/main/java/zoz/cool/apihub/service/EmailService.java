package zoz.cool.apihub.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import zoz.cool.apihub.client.EmailClient;
import zoz.cool.apihub.constant.CommonConstant;
import zoz.cool.apihub.constant.EmailConstant;
import zoz.cool.apihub.dao.domain.ApihubAlipayOrder;
import zoz.cool.apihub.dao.domain.ApihubUser;
import zoz.cool.apihub.dao.service.ApihubUserService;
import zoz.cool.apihub.vo.StatsDataVo;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class EmailService {
    @Resource
    private TemplateEngine templateEngine;
    @Resource
    private EmailClient emailClient;
    @Resource
    private ApihubUserService apihubUserService;

    public void sendMailVerifyCode(String to, String code) {
        //创建邮件正文
        Context context = new Context();
        context.setVariable("expiredTime", CommonConstant.VERIFY_CODE_EXPIRED_TIME / 60);
        context.setVariable("verifyCode", Arrays.asList(code.split("")));
        //将模块引擎内容解析成html字符串
        String emailContent = templateEngine.process(EmailConstant.VERIFY_CODE_EMAIL_TEMPLATE, context);
        emailClient.sendHtmlMail(to, EmailConstant.VERIFY_CODE_SUBJECT, emailContent);
    }

    public void notifyOrderPayment(ApihubAlipayOrder order) {
        // 获取管理员列表
        List<String> admins = apihubUserService.getAdmins().stream().map(ApihubUser::getEmail).filter(StrUtil::isNotEmpty).toList();
        log.info("用户订单支付成功，通知管理员: {}", admins);
        if (CollUtil.isNotEmpty(admins)) {
            //创建邮件正文
            Context context = new Context();
            context.setVariable("order", order);
            context.setVariable("user", apihubUserService.getUserByUid(order.getUserId()));
            //将模块引擎内容解析成html字符串
            String emailContent = templateEngine.process(EmailConstant.ORDER_NOTIFY_EMAIL_TEMPLATE, context);
            emailClient.sendHtmlMail(StrUtil.join(",", admins), EmailConstant.ORDER_SUBJECT, emailContent);
        }
    }

    public void sendDailyReport(StatsDataVo dayVo, StatsDataVo weekVo, StatsDataVo monthVo, StatsDataVo yearVo, StatsDataVo allVo) {
        // 获取管理员列表
        List<String> admins = apihubUserService.getAdmins().stream().map(ApihubUser::getEmail).filter(StrUtil::isNotEmpty).toList();
        if (CollUtil.isNotEmpty(admins)) {
            log.info("发送每日报表: {}", admins);
            //创建邮件正文
            Context context = new Context();
            context.setVariable("dayReport", dayVo);
            context.setVariable("weekReport", weekVo);
            context.setVariable("monthReport", monthVo);
            context.setVariable("yearReport", yearVo);
            context.setVariable("allReport", allVo);

            //将模块引擎内容解析成html字符串
            String emailContent = templateEngine.process(EmailConstant.DAILY_REPORT_EMAIL_TEMPLATE, context);
            emailClient.sendHtmlMail(StrUtil.join(",", admins), String.format(EmailConstant.DAILY_REPORT_SUBJECT, LocalDate.now().minusDays(1)), emailContent);
        }
    }

    public void notifyInvCheckFailed(String content) {
        // 获取管理员列表
        List<String> admins = apihubUserService.getAdmins().stream().map(ApihubUser::getEmail).filter(StrUtil::isNotEmpty).toList();
        log.info("发票查验失败，通知管理员: {}", admins);
        if (CollUtil.isNotEmpty(admins)) {
            //创建邮件正文
            emailClient.sendSimpleMail(StrUtil.join(",", admins), "发票查验任务失败", content);
        }
    }
}
