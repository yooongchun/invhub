package zoz.cool.apihub.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.lang.Assert;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import zoz.cool.apihub.dao.domain.ApihubUser;
import zoz.cool.apihub.dao.service.ApihubUserService;

/**
 * 用户管理
 */
@Slf4j
@RestController
@ResponseBody
@RequestMapping("/user")
@Tag(name = "UserController", description = "用户管理")
@SaCheckLogin
public class UserController {
    @Resource
    private ApihubUserService apihubUserService;

    @Operation(summary = "用户信息", description = "获取用户信息")
    @GetMapping({"/info", "/"})
    public ApihubUser userInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        Assert.notNull(userId, "获取用户信息失败，请重新登录！");
        ApihubUser user = apihubUserService.getUserByUid(userId);
        Assert.notNull(user, "获取用户信息失败，请联系管理员！");
        user.setPassword(null);
        return user;
    }

    @Operation(summary = "退出登录", description = "退出登录")
    @PostMapping("/logout")
    public void logout() {
        StpUtil.logout();
    }
}
