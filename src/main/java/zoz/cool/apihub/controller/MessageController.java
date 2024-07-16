package zoz.cool.apihub.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import zoz.cool.apihub.dao.domain.ApihubMessage;
import zoz.cool.apihub.dao.service.ApihubMessageService;
import zoz.cool.apihub.service.UserService;

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

    @Operation(summary = "获取消息")
    @GetMapping("/{id}")
    public ApihubMessage get(@PathVariable Long id) {
        return apihubMessageService.getById(id);
    }

    @Operation(summary = "删除消息")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        apihubMessageService.removeById(id);
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
    public Page<ApihubMessage> list(
            @RequestParam(required = false) String query,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String user,
            @RequestParam(required = false) Integer type){
        return apihubMessageService.selectPage(page, pageSize, query, user, type);
    }
    @Operation(summary = "消息组")
    @GetMapping("/group/{id}")
    public List<ApihubMessage> group(@PathVariable Long id){
        return apihubMessageService.selectGroup(id);
    }
}
