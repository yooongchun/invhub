package zoz.cool.apihub.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import zoz.cool.apihub.dao.domain.ApihubMessage;
import zoz.cool.apihub.dao.domain.ApihubUser;
import zoz.cool.apihub.dao.service.ApihubMessageService;
import zoz.cool.apihub.dao.service.ApihubUserService;
import zoz.cool.apihub.enums.MessageTypeEnum;
import zoz.cool.apihub.exception.ApiException;
import zoz.cool.apihub.service.UserService;
import zoz.cool.apihub.vo.MessageVo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 消息管理
 */
@Slf4j
@RestController
@ResponseBody
@RequestMapping("/msg")
@Tag(name = "10.消息接口")
@SaCheckLogin
public class MessageController {
    @Resource
    private ApihubMessageService apihubMessageService;
    @Resource
    private UserService userService;
    @Resource
    private ApihubUserService apihubUserService;

    @Operation(summary = "获取消息")
    @GetMapping("/{id}")
    public MessageVo get(@PathVariable Long id) {
        MessageVo messageVo = new MessageVo();
        ApihubMessage msg = apihubMessageService.getById(id);
        BeanUtils.copyProperties(msg, messageVo);
        ApihubUser loginUser = userService.getLoginUser();
        messageVo.setUsername(loginUser.getUsername());
        messageVo.setRead(isMsgRead(msg, loginUser));
        return messageVo;
    }

    @Operation(summary = "已读消息")
    @PatchMapping("/{id}")
    public void markAsRead(@PathVariable Long id) {
        ApihubMessage message = apihubMessageService.getById(id);
        LocalDateTime now = LocalDateTime.now();
        MessageTypeEnum msgType = MessageTypeEnum.getByCode(message.getType());
        ApihubUser loginUser = userService.getLoginUser();
        switch (msgType) {
            case USER -> loginUser.setLastMsgReadTime(now);
            case SYSTEM -> loginUser.setLastNotifyReadTime(now);
            default -> throw new ApiException("未知消息类型");
        }
        apihubUserService.updateById(loginUser);
    }

    @Operation(summary = "已读通知")
    @PatchMapping("/mark-read")
    public void markNotifyAsRead() {
        ApihubUser loginUser = userService.getLoginUser();
        loginUser.setLastNotifyReadTime(LocalDateTime.now());
        apihubUserService.updateById(loginUser);
    }

    @Operation(summary = "删除消息")
    @DeleteMapping("/{ids}")
    public void delete(@PathVariable String ids) {
        List<ApihubMessage> msgList = apihubMessageService.listByIds(Arrays.asList(ids.split(",")));
        msgList.forEach(msg -> msg.setDeleted(1));
        apihubMessageService.updateBatchById(msgList);
    }

    @Operation(summary = "发送消息")
    @PostMapping("/")
    public void send(@RequestBody ApihubMessage message) {
        message.setUserId(userService.getLoginUser().getUid());
        apihubMessageService.save(message);
    }

    @Operation(summary = "更新消息")
    @PutMapping("/")
    public void update(@RequestBody ApihubMessage message) {
        apihubMessageService.updateById(message);
    }

    @Operation(summary = "消息列表")
    @GetMapping("/list")
    public Page<MessageVo> list(
            @RequestParam(required = false) String keywords,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String user,
            @RequestParam(required = false) Integer type) {
        Page<ApihubMessage> rawMsgPage = apihubMessageService.selectPage(page, pageSize, keywords, user, type);
        Page<MessageVo> newMsgPage = new Page<>(page, pageSize);
        newMsgPage.setTotal(rawMsgPage.getTotal());
        newMsgPage.setPages(rawMsgPage.getPages());
        newMsgPage.setSize(rawMsgPage.getSize());
        List<MessageVo> msgVos = new ArrayList<>();
        for (ApihubMessage msg : rawMsgPage.getRecords()) {
            MessageVo newMsg = new MessageVo();
            BeanUtils.copyProperties(msg, newMsg);
            ApihubUser loginUser = userService.getLoginUser();
            newMsg.setUsername(loginUser.getUsername());
            newMsg.setRead(isMsgRead(msg, loginUser));
            msgVos.add(newMsg);
        }
        newMsgPage.setRecords(msgVos);
        return newMsgPage;
    }

    @Operation(summary = "系统通知")
    @GetMapping("/list/notify")
    public List<ApihubMessage> notifyList() {
        LocalDateTime lastReadTime = userService.getLoginUser().getLastNotifyReadTime();
        return apihubMessageService.list(new QueryWrapper<ApihubMessage>().eq("type", MessageTypeEnum.SYSTEM.getCode()).gt("create_time", lastReadTime));
    }

    @Operation(summary = "消息组")
    @GetMapping("/group/{id}")
    public List<ApihubMessage> group(@PathVariable Long id) {
        return apihubMessageService.selectGroup(id);
    }

    private boolean isMsgRead(ApihubMessage msg, ApihubUser user) {
        MessageTypeEnum msgType = MessageTypeEnum.getByCode(msg.getType());
        LocalDateTime lastReadTime;
        switch (msgType) {
            case USER -> lastReadTime = user.getLastMsgReadTime();
            case SYSTEM -> lastReadTime = user.getLastNotifyReadTime();
            default -> throw new ApiException("未知消息类型");
        }
        return lastReadTime != null && msg.getCreateTime().isBefore(lastReadTime);
    }
}
