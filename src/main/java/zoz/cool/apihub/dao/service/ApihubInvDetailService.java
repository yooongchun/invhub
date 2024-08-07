package zoz.cool.apihub.dao.service;

import zoz.cool.apihub.dao.domain.ApihubInvDetail;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author zhayongchun
* @description 针对表【apihub_inv_detail(发票详情表)】的数据库操作Service
* @createDate 2024-08-07 12:32:03
*/
public interface ApihubInvDetailService extends IService<ApihubInvDetail> {
    ApihubInvDetail getByFileId(Long fileId);
}
