package zoz.cool.apihub.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "ocr.baidu")
public class BaiduOcrConfig {
    private String appId;
    private String appSecret;
    private String apiAuth;
    private String urlPattern;
    private String apiOcr;
}
