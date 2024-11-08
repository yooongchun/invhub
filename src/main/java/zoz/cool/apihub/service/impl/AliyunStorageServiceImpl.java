package zoz.cool.apihub.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import zoz.cool.apihub.config.AliyunOssConfig;
import zoz.cool.apihub.enums.HttpCode;
import zoz.cool.apihub.exception.ApiException;
import zoz.cool.apihub.service.StorageService;
import zoz.cool.apihub.utils.TimeUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;

@Component
@ConditionalOnProperty(value = "storage.type", havingValue = "aliyun")
@Slf4j
public class AliyunStorageServiceImpl implements StorageService {
    @Resource
    private OSS ossClient;
    @Resource
    private AliyunOssConfig aliyunOssConfig;
    private final static String savePathTmpl = "data/inv/%s/%s/%s";

    public String upload(MultipartFile file) {
        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            log.error("读取字节流失败", e);
            throw new ApiException(HttpCode.INTERNAL_ERROR, "读取字节流失败");
        }
        return upload(fileBytes, file.getOriginalFilename());
    }

    public String upload(byte[] fileBytes, String fileName) {
        String savePath = String.format(savePathTmpl, TimeUtil.getLocalDateFormatted(), StpUtil.getLoginIdAsLong(), fileName);
        log.info("upload file to {}", savePath);
        try {
            // 创建PutObjectRequest对象。
            PutObjectRequest putObjectRequest = new PutObjectRequest(aliyunOssConfig.getBucketName(), savePath, new ByteArrayInputStream(fileBytes));
            // 创建PutObject请求。
            PutObjectResult result = ossClient.putObject(putObjectRequest);
            return savePath;
        } catch (OSSException | ClientException e) {
            log.error("上传OSS失败", e);
        }
        return null;
    }

    public byte[] download(String remotePath) {
        try {
            // ossObject包含文件所在的存储空间名称、文件名称、文件元数据以及一个输入流。
            OSSObject ossObject = ossClient.getObject(aliyunOssConfig.getBucketName(), remotePath);
            // 读取文件内容。
            return ossObject.getObjectContent().readAllBytes();
        } catch (Throwable e) {
            log.error("下载文件失败", e);
            throw new ApiException(HttpCode.INTERNAL_ERROR, "下载文件失败");
        }
    }

    public String preview(String objectName) {
        try {
            // 生成签名URL。
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(aliyunOssConfig.getBucketName(), objectName, HttpMethod.GET);
            // 设置过期时间1小时
            Date expiration = new Date(new Date().getTime() + 3600 * 1000L);
            request.setExpiration(expiration);
            // 通过HTTP GET请求生成签名URL。
            String previewUrl = ossClient.generatePresignedUrl(request).toString();
            log.info("预览文件URL: {}", previewUrl);
            return previewUrl;
        } catch (OSSException | ClientException e) {
            log.error("预览文件失败", e);
            throw new ApiException(HttpCode.INTERNAL_ERROR, "预览文件失败");
        }
    }
}
