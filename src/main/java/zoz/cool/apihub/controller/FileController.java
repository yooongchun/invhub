package zoz.cool.apihub.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.lang.Assert;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import zoz.cool.apihub.config.AliyunOssConfig;
import zoz.cool.apihub.config.StorageConfig;
import zoz.cool.apihub.dao.domain.ApihubFileInfo;
import zoz.cool.apihub.dao.domain.ApihubUser;
import zoz.cool.apihub.dao.service.ApihubFileInfoService;
import zoz.cool.apihub.enums.HttpCode;
import zoz.cool.apihub.exception.ApiException;
import zoz.cool.apihub.service.StorageService;
import zoz.cool.apihub.service.UserService;
import zoz.cool.apihub.utils.ToolKit;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Objects;

/**
 * 文件管理
 */
@Slf4j
@SaCheckLogin
@RestController
@ResponseBody
@RequestMapping("/file")
@Tag(name = "FileController", description = "文件管理")
public class FileController {
    @Resource
    private StorageService storageService;
    @Resource
    private ApihubFileInfoService apihubFileInfoService;
    @Resource
    private StorageConfig storageConfig;
    @Resource
    private AliyunOssConfig aliyunOssConfig;
    @Resource
    private UserService userService;

    @Operation(summary = "上传文件", description = "上传文件接口")
    @PostMapping("/upload")
    public ApihubFileInfo upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new ApiException(HttpCode.VALIDATE_FAILED, "文件不能为空");
        }
        // 类型限制
        if (!storageConfig.isFileContentTypeSupported(file.getContentType())) {
            throw new ApiException(HttpCode.VALIDATE_FAILED, "文件类型不支持，目前支持的文件类型有：" + storageConfig.getSupportedContentType());
        }
        //大小限制：单文件不能超过xx M
        if (!storageConfig.isFileSizeValid(file.getSize())) {
            throw new ApiException(HttpCode.VALIDATE_FAILED, "文件大小超过限制，最大支持" + storageConfig.getSizeLimit() + "MiB");
        }
        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            log.error("读取字节流失败", e);
            throw new ApiException(HttpCode.INTERNAL_ERROR, "读取字节流失败");
        }
        String fileHash = ToolKit.calFileHash(new ByteArrayInputStream(fileBytes));
        // 已存在则不能重复上传
        if (apihubFileInfoService.getByFileHash(fileHash) != null) {
            throw new ApiException(HttpCode.VALIDATE_FAILED, "文件已存在");
        }
        ApihubFileInfo fileInfo = new ApihubFileInfo();
        fileInfo.setUserId(StpUtil.getLoginIdAsLong());
        fileInfo.setFileName(file.getOriginalFilename());
        fileInfo.setFileHash(ToolKit.calFileHash(new ByteArrayInputStream(fileBytes)));
        fileInfo.setFileType(file.getContentType());
        fileInfo.setFileSize(file.getSize());
        fileInfo.setBucketName(aliyunOssConfig.getBucketName());
        try {
            String savePath = storageService.upload(file);
            fileInfo.setObjectName(savePath);
            apihubFileInfoService.save(fileInfo);
            log.info("上传文件成功: {}", fileInfo);
            return fileInfo;
        } catch (Exception e) {
            log.error("上传文件失败", e);
            throw new ApiException(HttpCode.INTERNAL_ERROR, "上传文件失败");
        }
    }

    @Operation(summary = "下载文件", description = "下载文件接口")
    @GetMapping("/{fileId}")
    public ResponseEntity<InputStreamResource> download(@PathVariable Long fileId) {
        ApihubFileInfo fileInfo = apihubFileInfoService.getById(fileId);
        Assert.notNull(fileInfo, "文件不存在");
        // 校验是否有权限
        ApihubUser user = userService.getLoginUser();
        if (!Objects.equals(user.getUid(), fileInfo.getUserId()) && user.getAdmin() == 0) {
            throw new ApiException(HttpCode.FORBIDDEN);
        }
        // 下载
        try {
            byte[] fileBytes = storageService.download(fileInfo.getObjectName());
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileInfo.getFileName());
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(new ByteArrayInputStream(fileBytes)));
        } catch (Exception e) {
            log.error("下载文件失败", e);
            throw new ApiException(HttpCode.INTERNAL_ERROR, "下载文件失败");
        }
    }
}