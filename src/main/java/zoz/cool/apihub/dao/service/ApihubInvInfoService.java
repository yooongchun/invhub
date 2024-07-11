package zoz.cool.apihub.dao.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import zoz.cool.apihub.dao.domain.ApihubInvInfo;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author yczha
 * @description 针对表【apihub_invoice_info(发票信息表)】的数据库操作Service
 * @createDate 2024-06-29 18:16:04
 */
public interface ApihubInvInfoService extends IService<ApihubInvInfo> {

    Page<ApihubInvInfo> list(Long userId, boolean isAdmin, Integer page, Integer pageSize, Integer checked, Integer reimbursed, LocalDate startDate, LocalDate endDate, String keywords, BigDecimal minMoney, BigDecimal maxMoney);

    ApihubInvInfo getByFileId(Long fileId);

}
