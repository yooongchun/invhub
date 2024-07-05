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
        try {
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
        } catch (Exception e) {
            log.error("发送邮件失败", e);
        }
    }
}
