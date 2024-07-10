package zoz.cool.apihub.controller;


import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.wf.captcha.SpecCaptcha;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import zoz.cool.apihub.client.RedisClient;
import zoz.cool.apihub.constant.CommonConstant;
import zoz.cool.apihub.dao.domain.ApihubUser;
import zoz.cool.apihub.dao.service.ApihubUserService;
import zoz.cool.apihub.enums.HttpCode;
import zoz.cool.apihub.exception.ApiException;
import zoz.cool.apihub.service.EmailService;
import zoz.cool.apihub.service.SmsService;
import zoz.cool.apihub.service.UserService;
import zoz.cool.apihub.utils.ToolKit;
import zoz.cool.apihub.vo.*;

import java.util.Objects;

/**
 * 注册管理
 */
@Slf4j
@RestController
@ResponseBody
@RequestMapping("/auth")
@Tag(name = "01.认证中心")
public class AuthController {
    @Resource
    private RedisClient redisClient;
    @Resource
    private EmailService emailService;
    @Resource
    private SmsService smsService;
    @Resource
    private ApihubUserService apihubUserService;
    @Resource
    private UserService userService;

    @Value("${sa-token.token-name}")
    private String tokenName;

    @Operation(summary = "用户注册", description = "用户注册接口")
    @PostMapping("/register")
    @Transactional
    public LoginResVo register(@Validated @RequestBody RegisterVo registerVo) {
        ApihubUser apihubUser = new ApihubUser();
        BeanUtils.copyProperties(registerVo, apihubUser);
        // 用户名是否已存在
        if (apihubUserService.getUser(apihubUser.getUsername()) != null) {
            throw new ApiException("该用户名已存在");
        }
        // 邮箱号或手机号是否已存在
        String email = apihubUser.getEmail();
        if (StrUtil.isNotEmpty(email) && apihubUserService.getUser(email) != null) {
            // 邮箱已存在
            throw new ApiException("该邮箱已注册");
        }
        String phone = apihubUser.getPhone();
        if (StrUtil.isNotEmpty(phone) && apihubUserService.getUser(phone) != null) {
            // 手机号已存在
            throw new ApiException("该手机号已注册");
        }
        if (StrUtil.isEmpty(email) && StrUtil.isEmpty(phone)) {
            throw new ApiException(HttpCode.VALIDATE_FAILED, "邮箱和手机号不能同时为空");
        }
        if (!registerVo.getPassword().equals(registerVo.getPasswordAgain())) {
            throw new ApiException(HttpCode.VALIDATE_FAILED, "两次密码不一致");
        }
        String key = StrUtil.isNotEmpty(email) ? email : phone;
        String storeCode = getStoreCode(key);
        if (StrUtil.isEmpty(storeCode) || !storeCode.equals(registerVo.getVerifyCode())) {
            throw new ApiException(HttpCode.VALIDATE_FAILED, "验证码不正确或已过期");
        }
        apihubUser.setPassword(ToolKit.getEncryptPassword(registerVo.getPassword()));
        apihubUser.setUid(ToolKit.getUid());
        apihubUserService.save(apihubUser);

        StpUtil.login(apihubUser.getUid());
        // 获取当前登录用户Token信息
        SaTokenInfo saTokenInfo = StpUtil.getTokenInfo();
        userService.insertLoginLog(apihubUser, ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest());
        return new LoginResVo(saTokenInfo.tokenValue, tokenName, saTokenInfo.tokenTimeout);
    }

    @Operation(summary = "图形验证码", description = "图形验证码接口")
    @GetMapping("/captcha")
    public CaptchaVo captcha() {
        SpecCaptcha specCaptcha = new SpecCaptcha(130, 48, 5);
        String verCode = specCaptcha.text().toLowerCase();
        // 生成一个uuid作为key
        String key = ToolKit.getUUID();
        // 存入redis并设置过期时间
        setStoreCode(key, verCode);
        // 将key和base64返回给前端
        return new CaptchaVo(key, specCaptcha.toBase64());
    }

    @Operation(summary = "验证码", description = "验证码接口")
    @PostMapping("/verify-code")
    public String verifyCode(@Validated @RequestBody VerifyCodeVo verifyCodeVo) {
        log.info("请求体：{}", verifyCodeVo);
        if (verifyCodeVo.getPhone() == null && verifyCodeVo.getEmail() == null) {
            throw new ApiException(HttpCode.VALIDATE_FAILED, "邮箱和手机号不能同时为空");
        }
        String capCode = getStoreCode(verifyCodeVo.getCaptchaKey());
        if (StrUtil.isEmpty(capCode) || !capCode.equals(verifyCodeVo.getCaptchaCode().toLowerCase())) {
            throw new ApiException(HttpCode.VALIDATE_FAILED, "图形验证码不正确或已过期");
        }
        String key = verifyCodeVo.getPhone() == null ? verifyCodeVo.getEmail() : verifyCodeVo.getPhone();
        // 生成一个6位数的随机验证码
        String verCode = ToolKit.getRandomCode();
        // 存入redis并设置过期时间
        setStoreCode(key, verCode);
        if (StrUtil.isNotEmpty(verifyCodeVo.getEmail())) {
            emailService.sendMailVerifyCode(verifyCodeVo.getEmail(), verCode);
            return "邮件已发送到您的邮箱，请查收！";
        } else {
            smsService.sendSmsVerifyCode(verifyCodeVo.getPhone(), verCode);
            return "短信已发送到您的手机，请查收！";
        }
    }

    @Operation(summary = "用户登录", description = "用户登录接口")
    @PostMapping("/login")
    public LoginResVo login(@Validated @RequestBody LoginVo loginVo) {
        ApihubUser user;
        String userKey = loginVo.getUsername();
        if (StrUtil.isEmpty(userKey)) {
            userKey = StrUtil.isEmpty(loginVo.getEmail()) ? loginVo.getPhone() : loginVo.getEmail();
        }
        if (StrUtil.isEmpty(userKey)) {
            throw new ApiException(HttpCode.VALIDATE_FAILED, "用户名、邮箱或手机号不能为空");
        }
        String password = loginVo.getPassword();
        String verifyCode = loginVo.getVerifyCode();

        if (password != null) { // 用户名+密码登录，需要验证图形验证码
            String captchaKey = loginVo.getCaptchaKey();
            if (StrUtil.isEmpty(captchaKey)) {
                throw new ApiException(HttpCode.VALIDATE_FAILED, "验证码key不能为空");
            }
            String storeCaptchaCode = getStoreCode(captchaKey);
            if (StrUtil.isEmpty(storeCaptchaCode) || !storeCaptchaCode.equals(loginVo.getCaptchaCode())) {
                throw new ApiException(HttpCode.VALIDATE_FAILED, "验证码不正确或已过期，请重新获取！");
            }
            // 验证码只能使用一次
            delStoreCode(captchaKey);
            user = apihubUserService.getUser(userKey);
            if (user == null || !ToolKit.checkPassword(password, user.getPassword())) {
                throw new ApiException(HttpCode.VALIDATE_FAILED, "用户名或密码错误");
            }
        } else if (StrUtil.isNotEmpty(verifyCode)) {// 邮箱或手机号+验证码登录
            String storeVerifyCode = getStoreCode(userKey);
            if (StrUtil.isEmpty(storeVerifyCode) || !storeVerifyCode.equals(verifyCode)) {
                throw new ApiException(HttpCode.VALIDATE_FAILED, "验证码不正确或已过期，请重新获取！");
            }
            user = apihubUserService.getUser(userKey);
        } else {
            throw new ApiException(HttpCode.VALIDATE_FAILED, "参数错误");
        }
        Assert.notNull(user, "登录异常，用户信息不存在！");
        // 密码校验成功后登录，一行代码实现登录
        StpUtil.login(user.getUid());
        // 获取当前登录用户Token信息
        SaTokenInfo saTokenInfo = StpUtil.getTokenInfo();
        userService.insertLoginLog(user, ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest());
        return new LoginResVo(saTokenInfo.tokenValue, tokenName, saTokenInfo.tokenTimeout);
    }

    /**
     * 找回密码
     */
    @Operation(summary = "找回密码", description = "找回密码")
    @PostMapping("/forget-password")
    public void changePassword(@Validated @RequestBody RegisterVo changePasswordVo) {
        ApihubUser user = apihubUserService.getUser(changePasswordVo.getUsername());
        if (user == null) {
            throw new ApiException(HttpCode.VALIDATE_FAILED, "用户不存在");
        }
        if (!changePasswordVo.getPassword().equals(changePasswordVo.getPasswordAgain())) {
            throw new ApiException(HttpCode.VALIDATE_FAILED, "两次密码不一致");
        }
        String key = StrUtil.isNotEmpty(user.getEmail()) ? user.getEmail() : user.getPhone();
        if (key == null) {
            throw new ApiException(HttpCode.VALIDATE_FAILED, "用户邮箱或手机号不存在");
        }
        String storeCode = getStoreCode(key);
        if (StrUtil.isEmpty(storeCode) || !storeCode.equals(changePasswordVo.getVerifyCode())) {
            throw new ApiException(HttpCode.VALIDATE_FAILED, "验证码不正确或已过期");
        }
        user.setPassword(ToolKit.getEncryptPassword(changePasswordVo.getPassword()));
        apihubUserService.updateById(user);
    }

    @Operation(summary = "用户是否已存在")
    @GetMapping("/user-exists")
    @Cacheable(value = "userExists", key = "#userKey", unless = "#result == false")
    public boolean userExists(@RequestParam String userKey) {
        return apihubUserService.getUser(userKey) != null;
    }

    private String getStoreCode(String key) {
        return (String) redisClient.get(CommonConstant.VERIFY_CODE_KEY_PREFIX + key);
    }

    private void setStoreCode(String key, String code) {
        redisClient.set(CommonConstant.VERIFY_CODE_KEY_PREFIX + key, code, CommonConstant.VERIFY_CODE_EXPIRED_TIME);
    }

    private void delStoreCode(String key) {
        redisClient.del(CommonConstant.VERIFY_CODE_KEY_PREFIX + key);
    }
}
