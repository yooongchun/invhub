package zoz.cool.apihub.dao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import zoz.cool.apihub.dao.domain.ApihubUser;
import zoz.cool.apihub.dao.mapper.ApihubUserMapper;
import zoz.cool.apihub.dao.service.ApihubUserService;

/**
 * @author yczha
 * @description 针对表【apihub_user(用户表)】的数据库操作Service实现
 * @createDate 2024-06-29 18:16:04
 */
@Service
public class ApihubUserServiceImpl extends ServiceImpl<ApihubUserMapper, ApihubUser> implements ApihubUserService {
    @Override
    public ApihubUser getUser(String key) {
        QueryWrapper<ApihubUser> query = new QueryWrapper<>();
        query.eq("username", key);
        query.or().eq("email", key);
        query.or().eq("phone", key);
        return getOne(query);
    }

    @Override
    public ApihubUser getUserByUid(Long uid) {
        return getOne(new QueryWrapper<ApihubUser>().eq("uid", uid));
    }
}




