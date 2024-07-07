package zoz.cool.apihub.dao.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import zoz.cool.apihub.dao.domain.ApihubLoginLog;
import zoz.cool.apihub.dao.mapper.ApihubLoginLogMapper;
import zoz.cool.apihub.dao.service.ApihubLoginLogService;

import java.time.LocalDate;

/**
 * @author yczha
 * @description 针对表【apihub_login_log(用户登录日志)】的数据库操作Service实现
 * @createDate 2024-06-29 18:16:04
 */
@Service
public class ApihubLoginLogServiceImpl extends ServiceImpl<ApihubLoginLogMapper, ApihubLoginLog> implements ApihubLoginLogService {

    public Page<ApihubLoginLog> listLogs(Integer pageNum, Integer pageSize, String kewWords, LocalDate startTime, LocalDate endTime) {
        Page<ApihubLoginLog> pageVo = new Page<>(pageNum, pageSize);
        QueryWrapper<ApihubLoginLog> query = new QueryWrapper<ApihubLoginLog>().ge(startTime != null, "create_time", startTime).le(endTime != null, "create_time", endTime);
        if (StrUtil.isNotBlank(kewWords)) {
            query.like("ip", kewWords).or().like("address", kewWords).or().like("user_agent", kewWords);
        }
        return baseMapper.selectPage(pageVo, query);
    }

}




