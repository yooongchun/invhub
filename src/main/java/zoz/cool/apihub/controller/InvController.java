package zoz.cool.apihub.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import zoz.cool.apihub.dao.domain.ApihubFileInfo;
import zoz.cool.apihub.dao.domain.ApihubInvoiceInfo;
import zoz.cool.apihub.dao.domain.ApihubProductPrice;
import zoz.cool.apihub.dao.domain.ApihubUser;
import zoz.cool.apihub.dao.service.ApihubFileInfoService;
import zoz.cool.apihub.dao.service.ApihubInvoiceInfoService;
import zoz.cool.apihub.dao.service.ApihubProductPriceService;
import zoz.cool.apihub.delegate.BaiduOcrDelegate;
import zoz.cool.apihub.enums.*;
import zoz.cool.apihub.exception.ApiException;
import zoz.cool.apihub.service.StorageService;
import zoz.cool.apihub.service.UserService;
import zoz.cool.apihub.utils.FileUtil;
import zoz.cool.apihub.utils.TimeUtil;
import zoz.cool.apihub.vo.BaiduOcrVo;
import zoz.cool.apihub.vo.InvInfoVo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 发票管理
 */
@RestController
@ResponseBody
@SaCheckLogin
@Tag(name = "05.发票管理")
@RequestMapping("/inv")
@Slf4j
public class InvController {
    @Resource
    private BaiduOcrDelegate baiduOcrDelegate;
    @Resource
    private ApihubFileInfoService apihubFileInfoService;
    @Resource
    private UserService userService;
    @Resource
    private StorageService storageService;
    @Resource
    private ApihubInvoiceInfoService apihubInvoiceInfoService;
    @Resource
    private ApihubProductPriceService apihubProductPriceService;

    @Operation(summary = "解析发票", description = "解析发票接口")
    @PostMapping("/parse")
    public ApihubInvoiceInfo parseInvData(@RequestParam Long fileId) {
        ApihubFileInfo fileInfo = apihubFileInfoService.getById(fileId);
        if (fileInfo == null) {
            log.warn("文件不存在，fileId={}", fileId);
            throw new ApiException(HttpCode.VALIDATE_FAILED, "文件不存在");
        }
        if (apihubInvoiceInfoService.getByFileId(fileId) != null) {
            throw new ApiException(HttpCode.VALIDATE_FAILED, "该文件已存在解析结果");
        }

        ApihubUser user = userService.getLoginUser();
        // 校验权限
        if (!Objects.equals(user.getUid(), fileInfo.getUserId()) && !userService.isAdmin()) {
            throw new ApiException(HttpCode.FORBIDDEN);
        }
        // 获取文件
        FileTypeEnum fileTypeEnum = FileTypeEnum.getFileType(fileInfo.getFileType());
        if (fileTypeEnum == null) {
            log.error("文件类型不存在：{}", fileInfo.getFileType());
            throw new ApiException(HttpCode.INTERNAL_ERROR);
        }

        // 计算费用
        BigDecimal price = getInvParsePrice();
        if (!userService.isAdmin() && user.getBalance().compareTo(price) < 0) {
            throw new ApiException(HttpCode.BUSINESS_FAILED, "余额不足");
        }
        ApihubInvoiceInfo invoiceInfo = new ApihubInvoiceInfo();
        invoiceInfo.setFileId(fileInfo.getId());
        invoiceInfo.setUserId(user.getUid());
        invoiceInfo.setStatus(InvStatusEnum.PROCESSING.getCode());
        invoiceInfo.setMethod(InvMethodEnum.BAIDU.name());
        apihubInvoiceInfoService.save(invoiceInfo);

        byte[] fileBytes = storageService.download(fileInfo.getObjectName());
        String base64String;
        switch (fileTypeEnum) {
            case IMAGE_BMP, IMAGE_JPEG, IMAGE_PNG, IMAGE_WEBP -> base64String = FileUtil.img2base64(fileBytes);
            case PDF -> base64String = FileUtil.img2base64(FileUtil.pdf2Image(fileBytes).getFirst());
            default -> throw new ApiException(HttpCode.VALIDATE_FAILED, "文件类型不支持");
        }
        BaiduOcrVo baiduOcrVo = baiduOcrDelegate.getOcrData(base64String);
        if (baiduOcrVo == null) {
            invoiceInfo.setStatus(InvStatusEnum.FAILED.getCode());
            apihubInvoiceInfoService.updateById(invoiceInfo);
            throw new ApiException(HttpCode.BUSINESS_FAILED, "解析失败");
        }
        // 成功，保存数据
        invoiceInfo.setStatus(InvStatusEnum.SUCCEED.getCode());
        invoiceInfo.setInvCode(baiduOcrVo.getInvoiceCode());
        invoiceInfo.setInvNum(baiduOcrVo.getInvoiceNum());
        invoiceInfo.setInvDate(TimeUtil.parseLocalDate(baiduOcrVo.getInvoiceDate()));
        invoiceInfo.setInvChk(baiduOcrVo.getCheckCode());
        invoiceInfo.setInvMoney(BigDecimal.valueOf(baiduOcrVo.getTotalAmount()));
        invoiceInfo.setInvTax(baiduOcrVo.getTotalTax().toString());
        invoiceInfo.setInvTotal(baiduOcrVo.getAmountInFiguers().toString());
        invoiceInfo.setInvType(baiduOcrVo.getInvoiceType());
        invoiceInfo.setInvDetail(JSONUtil.toJsonStr(baiduOcrVo));
        apihubInvoiceInfoService.updateById(invoiceInfo);
        // 扣费
        if (!userService.isAdmin()) {
            userService.deduceBalance(user, price, ProductNameEnum.INV_PARSE);
        }
        return invoiceInfo;
    }

    @Operation(summary = "获取发票信息", description = "获取发票信息接口")
    @GetMapping("/{invId}")
    public InvInfoVo getInvInfo(@PathVariable Long invId) {
        ApihubInvoiceInfo invoiceInfo = apihubInvoiceInfoService.getById(invId);
        if (invoiceInfo == null) {
            throw new ApiException(HttpCode.VALIDATE_FAILED, "发票信息不存在");
        }
        ApihubUser user = userService.getLoginUser();
        // 校验权限
        if (!Objects.equals(user.getUid(), invoiceInfo.getUserId()) && !userService.isAdmin()) {
            throw new ApiException(HttpCode.FORBIDDEN);
        }
        InvInfoVo invInfoVo = new InvInfoVo();
        BeanUtils.copyProperties(invoiceInfo, invInfoVo, "invDetail");
        invInfoVo.setInvDetailVo(JSONUtil.toBean(invoiceInfo.getInvDetail(), BaiduOcrVo.class));
        return invInfoVo;
    }

    @Operation(summary = "通过文件获取发票信息", description = "通过文件获取发票信息接口")
    @GetMapping("/file/{fileId}")
    public InvInfoVo getByFileId(@PathVariable Long fileId) {
        ApihubInvoiceInfo invoiceInfo = apihubInvoiceInfoService.getByFileId(fileId);
        if (invoiceInfo == null) {
            throw new ApiException(HttpCode.VALIDATE_FAILED, "发票信息不存在");
        }
        ApihubUser user = userService.getLoginUser();
        // 校验权限
        if (!Objects.equals(user.getUid(), invoiceInfo.getUserId()) && !userService.isAdmin()) {
            throw new ApiException(HttpCode.FORBIDDEN);
        }
        InvInfoVo invInfoVo = new InvInfoVo();
        BeanUtils.copyProperties(invoiceInfo, invInfoVo, "invDetail");
        invInfoVo.setInvDetailVo(JSONUtil.toBean(invoiceInfo.getInvDetail(), BaiduOcrVo.class));
        return invInfoVo;
    }

    @Operation(summary = "按用户查发票信息", description = "按用户查询发票信息")
    @GetMapping("/list")
    public Page<InvInfoVo> invList(@RequestParam(required = false, defaultValue = "1") Integer page, @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        Page<ApihubInvoiceInfo> rawData = apihubInvoiceInfoService.listByUserId(StpUtil.getLoginIdAsLong(), page, pageSize);
        Page<InvInfoVo> pageData = new Page<>(page, pageSize);
        List<InvInfoVo> newRecords = new ArrayList<>();
        for (ApihubInvoiceInfo info : rawData.getRecords()) {
            InvInfoVo invInfoVo = new InvInfoVo();
            BeanUtils.copyProperties(info, invInfoVo, "invDetail");
            invInfoVo.setInvDetailVo(JSONUtil.toBean(info.getInvDetail(), BaiduOcrVo.class));
            newRecords.add(invInfoVo);
        }
        pageData.setRecords(newRecords);
        return pageData;
    }

    private BigDecimal getInvParsePrice() {
        ApihubProductPrice productPrice = apihubProductPriceService.getOne(new QueryWrapper<ApihubProductPrice>().eq("product_code", ProductNameEnum.INV_PARSE.name()));
        if (productPrice == null) {
            throw new ApiException(HttpCode.INTERNAL_ERROR, "产品定价不存在");
        }
        return productPrice.getPrice();
    }
}
