package zoz.cool.apihub.dao.service.impl;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import zoz.cool.apihub.dao.domain.ApihubUser;
import zoz.cool.apihub.dao.mapper.ApihubUserMapper;
import zoz.cool.apihub.dao.service.ApihubUserService;

import java.math.BigDecimal;
import java.util.List;

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

    public BigDecimal addBalance(Long uid, BigDecimal amount) {
        ApihubUser user = getUserByUid(uid);
        Assert.notNull(user, "用户不存在");
        // 用户余额增加
        user.setBalance(user.getBalance().add(amount));
        updateById(user);
        return user.getBalance();
    }

    public List<ApihubUser> getAdmins() {
        return list(new QueryWrapper<ApihubUser>().eq("admin", 1));
    }

    public Page<ApihubUser> listUser(Integer page, Integer size) {
        Page<ApihubUser> pageVo = new Page<>(page, size);
        return baseMapper.selectPage(pageVo, null);
    }
}




