package zoz.cool.apihub.client;


import jakarta.annotation.Resource;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import zoz.cool.apihub.config.EmailConfig;
import zoz.cool.apihub.constant.CommonConstant;
import zoz.cool.apihub.constant.EmailConstant;

import java.util.Arrays;

@Slf4j
@Service
public class EmailClient {
    @Resource
    private JavaMailSender mailSender;
    @Resource
    private TemplateEngine templateEngine;
    @Resource
    private EmailConfig emailConfig;

    public void sendMailVerifyCode(String to, String code) {
        //创建邮件正文
        Context context = new Context();
        context.setVariable("expiredTime", CommonConstant.VERIFY_CODE_EXPIRED_TIME / 60);
        context.setVariable("verifyCode", Arrays.asList(code.split("")));
        //将模块引擎内容解析成html字符串
        String emailContent = templateEngine.process(EmailConstant.TEMPLATE_FILENAME, context);
        sendHtmlMail(to, EmailConstant.SUBJECT, emailContent);
    }

    /**
     * 简单文本邮件
     *
     * @param to      收件人
     * @param subject 主题
     * @param content 内容
     */
    @Async
    public void sendSimpleMail(String to, String subject, String content) {
        //创建SimpleMailMessage对象
        SimpleMailMessage message = new SimpleMailMessage();
        //邮件接收人
        message.setFrom(emailConfig.getUsername());
        message.setTo(to);
        //邮件主题
        message.setSubject(subject);
        //邮件内容
        message.setText(content);
        log.info("开始发送simple邮件: to={}", to);
        //发送邮件
        mailSender.send(message);
    }

    /**
     * html邮件
     *
     * @param to      收件人,多个时参数形式 ："xxx@xxx.com,xxx@xxx.com,xxx@xxx.com"
     * @param subject 主题
     * @param content 内容
     */
    @Async
    public void sendHtmlMail(String to, String subject, String content) {
        //获取MimeMessage对象
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper;
        try {
            messageHelper = new MimeMessageHelper(message, true);
            //邮件接收人,设置多个收件人地址
            InternetAddress[] internetAddressTo = InternetAddress.parse(to);
            message.setFrom(emailConfig.getUsername());
            messageHelper.setTo(internetAddressTo);
            //邮件主题
            message.setSubject(subject);
            //邮件内容，html格式
            messageHelper.setText(content, true);
            //发送
            log.info("开始发送html邮件: from={}, to={}", emailConfig.getUsername(), to);
            mailSender.send(message);
            log.info("发送html邮件成功");
        } catch (Exception e) {
            log.error("发送邮件时发生异常！", e);
        }
    }
}