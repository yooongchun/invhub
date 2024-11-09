package zoz.cool.apihub.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import zoz.cool.apihub.dao.domain.ApihubUser;
import zoz.cool.apihub.dao.domain.ApihubUserSettings;
import zoz.cool.apihub.dao.service.ApihubUserService;
import zoz.cool.apihub.dao.service.ApihubUserSettingsService;
import zoz.cool.apihub.enums.HttpCode;
import zoz.cool.apihub.enums.UserSettingEnum;
import zoz.cool.apihub.exception.ApiException;
import zoz.cool.apihub.service.UserService;
import zoz.cool.apihub.utils.ToolKit;
import zoz.cool.apihub.vo.ChangePasswordVo;

import java.time.LocalDate;

/**
 * 用户管理
 */
@Slf4j
@RestController
@ResponseBody
@RequestMapping("/user")
@Tag(name = "02.用户接口")
@SaCheckLogin
public class UserController {
    @Resource
    private ApihubUserService apihubUserService;
    @Resource
    private UserService userService;
    @Resource
    private ApihubUserSettingsService userSettingsService;


    @Operation(summary = "用户信息", description = "获取用户信息")
    @GetMapping({"/info", "/"})
    public ApihubUser userInfo() {
        ApihubUser user = userService.getLoginUser();
        Assert.notNull(user, "获取用户信息失败，请联系管理员！");
        user.setPassword(null);
        return user;
    }

    @Operation(summary = "获取指定用户信息")
    @GetMapping("/{id}")
    public ApihubUser userInfoById(@PathVariable Long id) {
        userService.checkAdmin();
        ApihubUser user = apihubUserService.getById(id);
        Assert.notNull(user, "获取用户信息失败，请联系管理员！");
        return user;
    }

    @Operation(summary = "修改用户信息")
    @PutMapping("/")
    public void updateUser(@RequestBody ApihubUser userVo) {
        userService.checkAdmin();
        ApihubUser user = apihubUserService.getById(userVo.getId());
        Assert.notNull(user, "获取用户信息失败，请联系管理员！");
        BeanUtils.copyProperties(userVo, user, "password");
        apihubUserService.updateById(user);
    }

    @Operation(summary = "修改用户密码")
    @PatchMapping("/{id}/change-password")
    public void changePassword(@PathVariable Long id, ChangePasswordVo changePasswordVo) {
        userService.checkAdmin();
        ApihubUser user = apihubUserService.getById(id);
        Assert.notNull(user, "获取用户信息失败，请联系管理员！");
        user.setPassword(ToolKit.getEncryptPassword(changePasswordVo.getNewPassword()));
        apihubUserService.updateById(user);
    }

    @Operation(summary = "删除用户(标记删除)")
    @DeleteMapping("/{ids}")
    public void deleteUser(@PathVariable String ids) {
        userService.checkAdmin();
        String[] idList = ids.split(",");
        for (String id : idList) {
            ApihubUser user = apihubUserService.getById(Long.parseLong(id));
            Assert.notNull(user, "获取用户信息失败，请联系管理员！");
            user.setDeleted(1);
            apihubUserService.updateById(user);
        }
    }

    @Operation(summary = "解禁用户")
    @PatchMapping("/{id}/enable")
    public void enableUser(@PathVariable Long id) {
        userService.checkAdmin();
        ApihubUser user = apihubUserService.getById(id);
        Assert.notNull(user, "获取用户信息失败，请联系管理员！");
        user.setDeleted(0);
        apihubUserService.updateById(user);
    }

    @Operation(summary = "新增用户")
    @PostMapping("/")
    public void addUser(@RequestBody ApihubUser userVo) {
        userService.checkAdmin();
        String userKey = userVo.getUsername();
        String email = userVo.getEmail();
        String phone = userVo.getPhone();
        if (StrUtil.isNotEmpty(userKey) && apihubUserService.getUser(userKey) != null) {
            throw new ApiException("该用户名已存在");
        }
        if (StrUtil.isNotEmpty(email) && apihubUserService.getUser(email) != null) {
            throw new ApiException("该邮箱已注册");
        }
        if (StrUtil.isNotEmpty(phone) && apihubUserService.getUser(phone) != null) {
            throw new ApiException("该手机号已注册");
        }
        if (StrUtil.isEmpty(userVo.getPassword())) {
            userVo.setPassword("apihub");
        }
        userVo.setPassword(ToolKit.getEncryptPassword(userVo.getPassword()));
        userVo.setUid(ToolKit.getUid());
        apihubUserService.save(userVo);
    }

    @Operation(summary = "退出登录", description = "退出登录")
    @PostMapping("/logout")
    public void logout() {
        StpUtil.logout();
    }

    @Operation(summary = "用户列表", description = "获取用户列表")
    @GetMapping("/list")
    public Page<ApihubUser> listUser(@RequestParam(defaultValue = "1") Integer
                                             pageNum, @RequestParam(defaultValue = "10") Integer
                                             pageSize, @RequestParam(required = false, name = "keywords") String
                                             kewWords, @RequestParam(required = false, name = "deleted") Integer
                                             deleted, @RequestParam(required = false) LocalDate startTime, @RequestParam(required = false) LocalDate endTime) {
        userService.checkAdmin();
        return apihubUserService.listUser(pageNum, pageSize, kewWords, deleted, startTime, endTime);
    }

    @Operation(summary = "用户配置")
    @PostMapping("/setting")
    @SaCheckLogin
    public void setUserConfig(@RequestParam String key, @RequestParam String value) {
        ApihubUser user = userService.getLoginUser();
        UserSettingEnum configKey = UserSettingEnum.getByName(key);
        if (configKey == null) {
            throw new ApiException("配置项不存在:" + key);
        }
        ApihubUserSettings userSetting = userSettingsService.getOne(new QueryWrapper<ApihubUserSettings>().eq("config_key", key).eq("user_id", user.getUid()));
        if (userSetting == null) {
            userSetting = new ApihubUserSettings();
            userSetting.setUserId(user.getUid());
            userSetting.setConfigKey(configKey.name());
            userSetting.setConfigValue(value);
            userSettingsService.save(userSetting);
        } else if (!userSetting.getConfigValue().equals(value)) {
            userSetting.setConfigValue(value);
            userSettingsService.updateById(userSetting);
        }
    }
}