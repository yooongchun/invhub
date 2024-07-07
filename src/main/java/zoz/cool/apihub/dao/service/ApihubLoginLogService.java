package zoz.cool.apihub.dao.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import zoz.cool.apihub.dao.domain.ApihubLoginLog;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDate;

/**
* @author yczha
* @description 针对表【apihub_login_log(用户登录日志)】的数据库操作Service
* @createDate 2024-06-29 18:16:04
*/
public interface ApihubLoginLogService extends IService<ApihubLoginLog> {

    Page<ApihubLoginLog> listLogs(Integer pageNum, Integer pageSize, String kewWords, LocalDate startTime, LocalDate endTime);

}
