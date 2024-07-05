package zoz.cool.apihub.client;


import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import zoz.cool.apihub.config.SmsConfig;

/**
 * 短信发送
 */

@Slf4j
@Service
public class SmsClient {
    // 产品名称:云通信短信API产品,开发者无需替换
    private static final String product = "Dysmsapi"; // 无需修改
    //产品域名,开发者无需替换
    private static final String domain = "dysmsapi.aliyuncs.com"; // 无需修改
    private static final String regionId = "cn-hangzhou";
    private static final String endpointName = regionId;

    @Resource
    private SmsConfig smsConfig;

    @Async
    public void sendSms(String phoneNumbers, String templateParam) {
        // 可自助调整超时时间
        System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
        System.setProperty("sun.net.client.defaultReadTimeout", "10000");
        log.info("发送短信，手机号：{}，验证码：{}", phoneNumbers, templateParam);
        SendSmsResponse sendSmsResponse;
        try {
            // 初始化acsClient,暂不支持region化
            IClientProfile profile = DefaultProfile.getProfile(regionId, smsConfig.getAccessKeyId(), smsConfig.getAccessKeySecret());
            DefaultProfile.addEndpoint(endpointName, regionId, product, domain);
            sendSmsResponse = getSendSmsResponse(phoneNumbers, templateParam, profile);
        } catch (Exception ex) {
            log.error("短信发送失败", ex);
            return;
        }
        if (!"OK".equals(sendSmsResponse.getCode())) {
            log.error("短信发送失败：{}", sendSmsResponse.getMessage());
        }
    }


    private SendSmsResponse getSendSmsResponse(String phoneNumbers, String templateParam, IClientProfile profile) throws ClientException {
        IAcsClient acsClient = new DefaultAcsClient(profile);

        //组装请求对象-具体描述见控制台-文档部分内容
        SendSmsRequest request = new SendSmsRequest();
        //必填:待发送手机号
        request.setPhoneNumbers(phoneNumbers);
        //必填:短信签名-可在短信控制台中找到
        request.setSignName(smsConfig.getSignName());
        //必填:短信模板-可在短信控制台中找到
        request.setTemplateCode(smsConfig.getTemplateCode());
        //可选:模板中的变量替换JSON串,如模板内容为"亲爱的${name},您的验证码为${title}"时,此处的值为
        request.setTemplateParam(templateParam);
        return acsClient.getAcsResponse(request);
    }
}
