package zoz.cool.apihub.dao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import zoz.cool.apihub.dao.domain.ApihubInvoiceInfo;
import zoz.cool.apihub.dao.domain.ApihubUser;
import zoz.cool.apihub.dao.service.ApihubInvoiceInfoService;
import zoz.cool.apihub.dao.mapper.ApihubInvoiceInfoMapper;
import org.springframework.stereotype.Service;
import zoz.cool.apihub.dao.service.ApihubUserService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yczha
 * @description 针对表【apihub_invoice_info(发票信息表)】的数据库操作Service实现
 * @createDate 2024-06-29 18:16:04
 */
@Service
public class ApihubInvoiceInfoServiceImpl extends ServiceImpl<ApihubInvoiceInfoMapper, ApihubInvoiceInfo>
        implements ApihubInvoiceInfoService {
    @Resource
    private ApihubUserService apihubUserService;

    public Page<ApihubInvoiceInfo> listByUserId(Long userId, boolean isAdmin, Integer page, Integer pageSize,
                                                Integer status, Integer checked, Integer reimbursed, LocalDate startDate, LocalDate endDate, String keywords,
                                                BigDecimal minMoney, BigDecimal maxMoney) {
        Page<ApihubInvoiceInfo> pageData = new Page<>(page, pageSize);
        QueryWrapper<ApihubInvoiceInfo> query = new QueryWrapper<>();
        query.eq(status != null, "status", status);
        query.eq(checked != null, "checked", checked);
        query.eq(reimbursed != null, "reimbursed", reimbursed);
        query.ge(startDate != null, "inv_date", startDate);
        query.le(endDate != null, "inv_date", endDate);
        List<Long> userIds = new ArrayList<>();
        if (keywords != null) {
            List<ApihubUser> userList = apihubUserService.list(new QueryWrapper<ApihubUser>().like("username", keywords));
            userList.forEach(user -> userIds.add(user.getUid()));
        }
        query.like(keywords != null, "inv_code", keywords)
                .or().like(keywords != null, "inv_num", keywords)
                .or().like(keywords != null, "inv_type", keywords);
        query.in(!userIds.isEmpty(), "user_id", userIds);
        query.eq(!isAdmin, "user_id", userId);
        pageData = baseMapper.selectPage(pageData, query);
        return pageData;
    }

    public ApihubInvoiceInfo getByFileId(Long fileId) {
        return getOne(new QueryWrapper<ApihubInvoiceInfo>().eq("file_id", fileId));
    }
}
