package zoz.cool.apihub.dao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import zoz.cool.apihub.dao.domain.ApihubInvoiceInfo;
import zoz.cool.apihub.dao.service.ApihubInvoiceInfoService;
import zoz.cool.apihub.dao.mapper.ApihubInvoiceInfoMapper;
import org.springframework.stereotype.Service;

/**
 * @author yczha
 * @description 针对表【apihub_invoice_info(发票信息表)】的数据库操作Service实现
 * @createDate 2024-06-29 18:16:04
 */
@Service
public class ApihubInvoiceInfoServiceImpl extends ServiceImpl<ApihubInvoiceInfoMapper, ApihubInvoiceInfo>
        implements ApihubInvoiceInfoService {

    public Page<ApihubInvoiceInfo> listByUserId(Long userId, Integer page, Integer pageSize) {
        Page<ApihubInvoiceInfo> pageData = new Page<>(page, pageSize);
        pageData = baseMapper.selectPage(pageData, new QueryWrapper<ApihubInvoiceInfo>().eq("user_id", userId));
        return pageData;
    }

    public ApihubInvoiceInfo getByFileId(Long fileId) {
        return getOne(new QueryWrapper<ApihubInvoiceInfo>().eq("file_id", fileId));
    }
}
