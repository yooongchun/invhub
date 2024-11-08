package zoz.cool.apihub.dao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import zoz.cool.apihub.dao.domain.ApihubInvCheckTask;
import zoz.cool.apihub.dao.domain.ApihubInvDetail;
import zoz.cool.apihub.dao.service.ApihubInvCheckTaskService;
import zoz.cool.apihub.dao.mapper.ApihubInvCheckTaskMapper;
import org.springframework.stereotype.Service;

/**
 * @author zhayongchun
 * @description 针对表【apihub_inv_check_task(发票查验任务表)】的数据库操作Service实现
 * @createDate 2024-11-08 17:39:46
 */
@Service
public class ApihubInvCheckTaskServiceImpl extends ServiceImpl<ApihubInvCheckTaskMapper, ApihubInvCheckTask>
        implements ApihubInvCheckTaskService {

    @Override
    public ApihubInvCheckTask getInvCheckTaskByInvIdUid(Long invId, Long userId) {
        return getOne(new QueryWrapper<ApihubInvCheckTask>().eq("inv_id", invId).eq("user_id", userId));
    }
}




