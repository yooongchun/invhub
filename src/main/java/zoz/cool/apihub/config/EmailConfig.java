package zoz.cool.apihub.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("spring.mail")
@Data
public class EmailConfig {
    private String host;
    private int port;
    private String username;
    private String password;
}
