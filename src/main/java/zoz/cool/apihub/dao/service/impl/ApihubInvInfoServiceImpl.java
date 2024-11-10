package zoz.cool.apihub.dao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import zoz.cool.apihub.dao.domain.ApihubInvCheckTask;
import zoz.cool.apihub.dao.domain.ApihubInvInfo;
import zoz.cool.apihub.dao.domain.ApihubUser;
import zoz.cool.apihub.dao.mapper.ApihubInvInfoMapper;
import zoz.cool.apihub.dao.service.ApihubInvCheckTaskService;
import zoz.cool.apihub.dao.service.ApihubInvInfoService;
import org.springframework.stereotype.Service;
import zoz.cool.apihub.dao.service.ApihubUserService;
import zoz.cool.apihub.enums.HttpCode;
import zoz.cool.apihub.exception.ApiException;

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
@Slf4j
public class ApihubInvInfoServiceImpl extends ServiceImpl<ApihubInvInfoMapper, ApihubInvInfo>
        implements ApihubInvInfoService {
    @Resource
    private ApihubUserService apihubUserService;
    @Resource
    private ApihubInvCheckTaskService invCheckTaskService;

    public Page<ApihubInvInfo> list(Long userId, boolean isAdmin, Integer page, Integer pageSize, Integer checked, Integer reimbursed, LocalDate startDate, LocalDate endDate, String keywords,
                                    BigDecimal minMoney, BigDecimal maxMoney, Integer invChecked) {
        Page<ApihubInvInfo> pageData = new Page<>(page, pageSize);
        QueryWrapper<ApihubInvInfo> query = new QueryWrapper<>();
        query.eq(checked != null, "checked", checked);
        query.eq(reimbursed != null, "reimbursed", reimbursed);
        query.ge(startDate != null, "inv_date", startDate);
        query.le(endDate != null, "inv_date", endDate);
        query.gt(minMoney != null, "amount", minMoney);
        query.le(maxMoney != null, "amount", maxMoney);
        List<Long> userIds = new ArrayList<>();
        if (keywords != null) {
            List<ApihubUser> userList = apihubUserService.list(new QueryWrapper<ApihubUser>().like("username", keywords));
            userList.forEach(user -> userIds.add(user.getUid()));
            query.and(i -> i.like("inv_code", keywords).or().like("inv_num", keywords).or().like("inv_type", keywords));
        }
        query.in(!userIds.isEmpty(), "user_id", userIds);
        query.eq(!isAdmin, "user_id", userId);
        if (invChecked != null) {
            List<ApihubInvCheckTask> tasks = invCheckTaskService.list(new QueryWrapper<ApihubInvCheckTask>().eq("user_id", userId).eq("status", invChecked));
            List<Long> invIdList = new ArrayList<>();
            tasks.forEach(task -> invIdList.add(task.getInvId()));
            if (!invIdList.isEmpty()) {
                query.in("id", invIdList);
            } else {
                return new Page<>();
            }
        }
        pageData = baseMapper.selectPage(pageData, query);
        return pageData;
    }

    public ApihubInvInfo getByFileId(Long fileId) {
        ApihubInvInfo inv = getOne(new QueryWrapper<ApihubInvInfo>().eq("file_id", fileId));
        if (inv == null) {
            throw new ApiException(HttpCode.NOT_FOUND, "未找到对应的发票信息");
        }
        return inv;
    }
}
