package zoz.cool.apihub.dao.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import zoz.cool.apihub.dao.domain.ApihubMessage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author zhayongchun
* @description 针对表【apihub_message(消息表)】的数据库操作Service
* @createDate 2024-07-16 19:58:22
*/
public interface ApihubMessageService extends IService<ApihubMessage> {
    Page<ApihubMessage> selectPage(Integer page, Integer pageSize, String query, String username, Integer type);
    List<ApihubMessage> selectGroup(Long id);
}
