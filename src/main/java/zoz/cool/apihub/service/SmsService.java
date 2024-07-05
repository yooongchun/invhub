package zoz.cool.apihub.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import zoz.cool.apihub.client.SmsClient;
import zoz.cool.apihub.constant.EmailConstant;

@Service
public class SmsService {
    @Resource
    private SmsClient smsClient;

    public void sendSmsVerifyCode(String phoneNumbers, String code) {
        String templateParams = String.format(EmailConstant.TEMPLATE_PARAMS, code);
        smsClient.sendSms(phoneNumbers, templateParams);
    }
}
