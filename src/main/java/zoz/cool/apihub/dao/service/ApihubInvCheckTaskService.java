package zoz.cool.apihub.dao.service;

import zoz.cool.apihub.dao.domain.ApihubInvCheckTask;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author zhayongchun
* @description 针对表【apihub_inv_check_task(发票查验任务表)】的数据库操作Service
* @createDate 2024-11-08 17:39:46
*/
public interface ApihubInvCheckTaskService extends IService<ApihubInvCheckTask> {
    ApihubInvCheckTask getInvCheckTaskByInvIdUid(Long invId, Long userId);
}
