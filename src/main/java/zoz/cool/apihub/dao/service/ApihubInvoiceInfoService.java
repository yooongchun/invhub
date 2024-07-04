package zoz.cool.apihub.dao.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import zoz.cool.apihub.dao.domain.ApihubInvoiceInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author yczha
 * @description 针对表【apihub_invoice_info(发票信息表)】的数据库操作Service
 * @createDate 2024-06-29 18:16:04
 */
public interface ApihubInvoiceInfoService extends IService<ApihubInvoiceInfo> {

    Page<ApihubInvoiceInfo> listByUserId(Long userId, Integer page, Integer pageSize);

    ApihubInvoiceInfo getByFileId(Long fileId);

}
