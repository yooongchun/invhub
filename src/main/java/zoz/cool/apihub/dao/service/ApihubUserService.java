package zoz.cool.apihub.dao.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import zoz.cool.apihub.dao.domain.ApihubUser;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author yczha
 * @description 针对表【apihub_user(用户表)】的数据库操作Service
 * @createDate 2024-06-29 18:16:04
 */
public interface ApihubUserService extends IService<ApihubUser> {
    ApihubUser getUser(String key);

    ApihubUser getUserByUid(Long uid);

    BigDecimal addBalance(Long uid, BigDecimal amount);

    List<ApihubUser> getAdmins();

    Page<ApihubUser> listUser(Integer page, Integer size);
}
