package zoz.cool.apihub.dao.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import zoz.cool.apihub.dao.domain.ApihubInvoiceInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author yczha
 * @description 针对表【apihub_invoice_info(发票信息表)】的数据库操作Service
 * @createDate 2024-06-29 18:16:04
 */
public interface ApihubInvoiceInfoService extends IService<ApihubInvoiceInfo> {

    Page<ApihubInvoiceInfo> listByUserId(Long userId, boolean isAdmin, Integer page, Integer pageSize, Integer status, Integer checked, Integer reimbursed, LocalDate startDate, LocalDate endDate, String keywords, BigDecimal minMoney, BigDecimal maxMoney);

    ApihubInvoiceInfo getByFileId(Long fileId);

}
