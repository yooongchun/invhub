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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import zoz.cool.apihub.dao.domain.*;
import zoz.cool.apihub.dao.service.ApihubFileInfoService;
import zoz.cool.apihub.dao.service.ApihubInvDetailService;
import zoz.cool.apihub.dao.service.ApihubInvInfoService;
import zoz.cool.apihub.dao.service.ApihubProductPriceService;
import zoz.cool.apihub.dao.service.impl.ApihubUserServiceImpl;
import zoz.cool.apihub.delegate.BaiduOcrDelegate;
import zoz.cool.apihub.enums.*;
import zoz.cool.apihub.exception.ApiException;
import zoz.cool.apihub.service.InvService;
import zoz.cool.apihub.service.StorageService;
import zoz.cool.apihub.service.UserService;
import zoz.cool.apihub.utils.FileUtil;
import zoz.cool.apihub.utils.TimeUtil;
import zoz.cool.apihub.vo.BaiduOcrVo;
import zoz.cool.apihub.vo.InvInfoVo;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private ApihubInvInfoService apihubInvInfoService;
    @Resource
    private ApihubInvDetailService apihubInvDetailService;
    @Resource
    private ApihubProductPriceService apihubProductPriceService;
    @Resource
    private InvService invService;
    @Resource
    private ApihubUserServiceImpl apihubUserServiceImpl;

    @Operation(summary = "解析发票", description = "解析发票接口")
    @PostMapping("/detail/parse")
    @Transactional
    public ApihubInvDetail parseInvData(@RequestParam Long fileId) {
        ApihubFileInfo fileInfo = apihubFileInfoService.getById(fileId);
        if (fileInfo == null) {
            log.warn("文件不存在，fileId={}", fileId);
            throw new ApiException(HttpCode.VALIDATE_FAILED, "文件不存在");
        }
        ApihubUser user = userService.getLoginUser();
        // 校验权限
        if (!Objects.equals(fileInfo.getUserId(), user.getUid()) && !userService.isAdmin()) {
            throw new ApiException(HttpCode.FORBIDDEN);
        }
        ApihubInvDetail invDetail = apihubInvDetailService.getByFileId(fileId);
        // 文件+用户唯一确认一个任务
        if (invDetail != null && invDetail.getUserId().equals(user.getUid())) {
            return invDetail;
        }
        // 获取文件
        FileTypeEnum fileTypeEnum = FileTypeEnum.getFileType(fileInfo.getFileType());
        if (fileTypeEnum == null) {
            log.error("文件类型不存在：{}", fileInfo.getFileType());
            throw new ApiException(HttpCode.INTERNAL_ERROR);
        }

        // 计算费用，管理员不计费
        BigDecimal price = getInvParsePrice();
        if (!userService.isAdmin() && user.getBalance().compareTo(price) < 0) {
            throw new ApiException(HttpCode.BUSINESS_FAILED, "余额不足");
        }

        invDetail = new ApihubInvDetail();
        invDetail.setFileId(fileInfo.getId());
        invDetail.setUserId(user.getUid());
        invDetail.setStatus(InvStatusEnum.PROCESSING.getCode());
        invDetail.setMethod(InvMethodEnum.BAIDU.name());
        apihubInvDetailService.save(invDetail);

        byte[] fileBytes = storageService.download(fileInfo.getObjectName());
        String base64String;
        switch (fileTypeEnum) {
            case IMAGE_BMP, IMAGE_JPEG, IMAGE_PNG, IMAGE_WEBP -> base64String = FileUtil.img2base64(fileBytes);
            case PDF -> base64String = FileUtil.img2base64(FileUtil.pdf2Image(fileBytes).getFirst());
            default -> throw new ApiException(HttpCode.VALIDATE_FAILED, "文件类型不支持");
        }
        BaiduOcrVo baiduOcrVo = baiduOcrDelegate.getOcrData(base64String);
        if (baiduOcrVo == null) {
            invDetail.setStatus(InvStatusEnum.FAILED.getCode());
            apihubInvDetailService.updateById(invDetail);
            throw new ApiException(HttpCode.BUSINESS_FAILED, "解析失败");
        }
        // 成功，保存数据
        invDetail.setStatus(InvStatusEnum.SUCCEED.getCode());
        invService.convertAndCopyFields(baiduOcrVo, invDetail);
        invDetail.setExtra(JSONUtil.toJsonStr(baiduOcrVo));
        apihubInvDetailService.updateById(invDetail);

        // 扣费
        if (!userService.isAdmin()) {
            userService.deduceBalance(user, price, ProductNameEnum.INV_PARSE);
        }
        setInvInfo(invDetail);
        invDetail.setExtra(null);
        return invDetail;
    }

    @Operation(summary = "获取发票信息", description = "获取发票信息接口")
    @GetMapping("/info/{invId}")
    public ApihubInvInfo getInvInfo(@PathVariable Long invId) {
        ApihubInvInfo invInfo = apihubInvInfoService.getById(invId);
        if (invInfo == null) {
            throw new ApiException(HttpCode.VALIDATE_FAILED, "发票信息不存在");
        }
        ApihubUser user = userService.getLoginUser();
        // 校验权限
        if (!Objects.equals(user.getUid(), invInfo.getUserId()) && !userService.isAdmin()) {
            throw new ApiException(HttpCode.FORBIDDEN);
        }
        return invInfo;
    }

    @Operation(summary = "通过文件获取发票信息", description = "通过文件获取发票信息接口")
    @GetMapping("/info/file/{fileId}")
    public ApihubInvInfo getByFileId(@PathVariable Long fileId) {
        ApihubInvInfo invInfo = apihubInvInfoService.getByFileId(fileId);
        if (invInfo == null) {
            throw new ApiException(HttpCode.VALIDATE_FAILED, "发票信息不存在");
        }
        ApihubUser user = userService.getLoginUser();
        // 校验权限
        if (!Objects.equals(user.getUid(), invInfo.getUserId()) && !userService.isAdmin()) {
            throw new ApiException(HttpCode.FORBIDDEN);
        }
        return invInfo;
    }

    @Operation(summary = "发票列表")
    @GetMapping("/info/list")
    public Page<InvInfoVo> invList(@RequestParam(required = false, defaultValue = "1") Integer page, @RequestParam(required = false, defaultValue = "10") Integer pageSize, @RequestParam(required = false) String keywords, @RequestParam(required = false) Integer checked, @RequestParam(required = false) Integer reimbursed, @RequestParam(required = false) BigDecimal minAmount, @RequestParam(required = false) BigDecimal maxAmount, @RequestParam(required = false) LocalDate startTime, @RequestParam(required = false) LocalDate endTime) {
        Page<ApihubInvInfo> rawPageData = apihubInvInfoService.list(StpUtil.getLoginIdAsLong(), userService.isAdmin(), page, pageSize, checked, reimbursed, startTime, endTime, keywords, minAmount, maxAmount);
        Page<InvInfoVo> pageData = new Page<>(page, pageSize);
        pageData.setTotal(rawPageData.getTotal());
        pageData.setPages(rawPageData.getPages());

        List<InvInfoVo> records = new ArrayList<>();
        for (ApihubInvInfo invInfo : rawPageData.getRecords()) {
            InvInfoVo invInfoVo = new InvInfoVo();
            BeanUtils.copyProperties(invInfo, invInfoVo);
            // 报销人
            ApihubUser user = apihubUserServiceImpl.getUserByUid(invInfo.getUserId());
            invInfoVo.setOwner(user.getUsername());
            // 校验码只显示最后6位
            if (invInfo.getCheckCode() != null && invInfo.getCheckCode().length() >= 6) {
                invInfoVo.setCheckCode(invInfo.getCheckCode().substring(invInfo.getCheckCode().length() - 6));
            }
            // 查验状态
            // TODO: 查验结果在此更新
            invInfoVo.setInvChecked(InvCheckEnum.UNCHECKED.getCode());
            records.add(invInfoVo);
        }
        pageData.setRecords(records);
        return pageData;
    }

    private BigDecimal getInvParsePrice() {
        ApihubProductPrice productPrice = apihubProductPriceService.getOne(new QueryWrapper<ApihubProductPrice>().eq("product_code", ProductNameEnum.INV_PARSE.name()));
        if (productPrice == null) {
            throw new ApiException(HttpCode.INTERNAL_ERROR, "产品定价不存在");
        }
        return productPrice.getPrice();
    }

    /**
     * 根据详情生成info信息
     */
    private void setInvInfo(ApihubInvDetail invDetail) {
        ApihubInvInfo invInfo = new ApihubInvInfo();
        BeanUtils.copyProperties(invDetail, invInfo);
        invInfo.setInvDetailId(invDetail.getId());
        invInfo.setInvCode(invDetail.getInvoiceCode());
        invInfo.setInvNum(invDetail.getInvoiceNum());
        invInfo.setInvDate(TimeUtil.parseLocalDate(invDetail.getInvoiceDate()));
        InvTypeEnum invType = InvTypeEnum.getEnumByName(invDetail.getInvoiceType());
        invInfo.setInvType(invType.getCode());
        invInfo.setAmount(new BigDecimal(invDetail.getTotalAmount()));
        invInfo.setTax(invDetail.getTotalTax().toString());
        invInfo.setMethod(InvMethodEnum.AUTO.name());
        apihubInvInfoService.save(invInfo);
    }
}
