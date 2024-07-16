package zoz.cool.apihub.dao.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import zoz.cool.apihub.dao.domain.ApihubMessage;
import zoz.cool.apihub.dao.domain.ApihubUser;
import zoz.cool.apihub.dao.service.ApihubMessageService;
import zoz.cool.apihub.dao.mapper.ApihubMessageMapper;
import org.springframework.stereotype.Service;
import zoz.cool.apihub.dao.service.ApihubUserService;
import zoz.cool.apihub.exception.ApiException;
import zoz.cool.apihub.service.UserService;

import java.util.*;

/**
 * @author zhayongchun
 * @description 针对表【apihub_message(消息表)】的数据库操作Service实现
 * @createDate 2024-07-16 19:58:22
 */
@Service
public class ApihubMessageServiceImpl extends ServiceImpl<ApihubMessageMapper, ApihubMessage>
        implements ApihubMessageService {
    @Resource
    private UserService userService;
    @Resource
    private ApihubUserService apihubUserService;

    @Override
    public Page<ApihubMessage> selectPage(Integer page, Integer pageSize, String query, String username, Integer type) {
        ApihubUser user = null;
        if (StrUtil.isNotEmpty(username) && userService.isAdmin()) {
            user = apihubUserService.getUser(username);
            if (user == null) {
                throw new ApiException("用户不存在");
            }
        } else if (!userService.isAdmin()) {
            user = userService.getLoginUser();
        }

        Page<ApihubMessage> pageData = new Page<>(page, pageSize);
        QueryWrapper<ApihubMessage> queryWrapper = new QueryWrapper<>();
        if (user != null) {
            queryWrapper.eq("user_id", user.getUid());
        }
        queryWrapper.like(StrUtil.isNotEmpty(query), "text", query);
        queryWrapper.eq(type != null, "type", type);
        pageData = baseMapper.selectPage(pageData, queryWrapper);
        return pageData;
    }

    public List<ApihubMessage> selectGroup(Long id) {
        List<ApihubMessage> messages = new ArrayList<>();
        getMessageChain(id, messages);
        return messages;
    }

    // 递归获取消息链
    private void getMessageChain(Long id, List<ApihubMessage> messages) {
        // 找到源头的消息
        ApihubMessage message = baseMapper.selectById(id);
        while (message != null && message.getParentId() != null) {
            message = baseMapper.selectById(message.getParentId());
        }
        // 向下递归
        Stack<ApihubMessage> children = new Stack<>();
        children.add(message);
        while (!children.isEmpty()) {
            ApihubMessage child = children.pop();
            messages.add(child);
            List<ApihubMessage> elem = list(new QueryWrapper<ApihubMessage>().eq("parent_id", child.getId()));
            if (!elem.isEmpty()) {
                children.addAll(elem);
            }
        }
    }
}




