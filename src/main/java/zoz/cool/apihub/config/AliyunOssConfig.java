package zoz.cool.apihub.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "storage.aliyun")
public class AliyunOssConfig {
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    @Bean(name = "aliyunOssClient")
    public OSS aliyunOssClient() {
        // 构建并返回OSSClient
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }
}
