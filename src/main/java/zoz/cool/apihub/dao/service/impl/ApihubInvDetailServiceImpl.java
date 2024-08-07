package zoz.cool.apihub.dao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import zoz.cool.apihub.dao.domain.ApihubInvDetail;
import zoz.cool.apihub.dao.service.ApihubInvDetailService;
import zoz.cool.apihub.dao.mapper.ApihubInvDetailMapper;
import org.springframework.stereotype.Service;

/**
* @author zhayongchun
* @description 针对表【apihub_inv_detail(发票详情表)】的数据库操作Service实现
* @createDate 2024-08-07 12:32:03
*/
@Service
public class ApihubInvDetailServiceImpl extends ServiceImpl<ApihubInvDetailMapper, ApihubInvDetail>
        implements ApihubInvDetailService {
    public ApihubInvDetail getByFileId(Long fileId) {
        return getOne(new QueryWrapper<ApihubInvDetail>().eq("file_id", fileId));
    }

}




