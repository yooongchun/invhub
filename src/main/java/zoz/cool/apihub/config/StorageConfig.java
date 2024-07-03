package zoz.cool.apihub.config;

import cn.hutool.core.lang.Assert;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties("storage")
public class StorageConfig {
    private String supportedContentType;
    private String type;
    private Integer sizeLimit; // 单位：MiB

    public List<String> getSupportedContentType() {
        Assert.notEmpty(supportedContentType, "storage.supported-content-type 不能为空");
        return Arrays.asList(supportedContentType.split(","));
    }

    public boolean isFileContentTypeSupported(String contentType) {
        return getSupportedContentType().contains(contentType);
    }

    public boolean isFileSizeValid(long size) {
        // 考虑单位转换
        return size <= sizeLimit * 1024 * 1024;
    }
}
