package zoz.cool.apihub.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Data
@Configuration
@ConfigurationProperties("storage")
public class StorageConfig {
    private String type;
    private Integer sizeLimit; // 单位：MiB

    public boolean isFileSizeValid(long size) {
        // 考虑单位转换
        return size <= sizeLimit * 1024 * 1024;
    }
}
