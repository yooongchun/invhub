package zoz.cool.apihub.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("spring.sms")
@Data
public class SmsConfig {
    private String accessKeyId;
    private String accessKeySecret;
    private String signName;
    private String templateCode;
}
