package zoz.cool.apihub.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 用户注册参数 Created by zhayongchun on 2023/11/17.
 */
@Data
public class RegisterVo {
    @NotEmpty(message = "用户名不能为空")
    @Schema(description = "用户名")
    private String username;

    @NotEmpty(message = "密码不能为空")
    @Pattern(regexp = "^\\w{6,20}$", message = "密码必须为6~20位！")
    @Schema(description = "密码")
    private String password;

    @NotEmpty(message = "密码复核不能为空")
    @Pattern(regexp = "^\\w{6,20}$", message = "密码必须为6~20位！")
    @Schema(description = "密码复核")
    private String passwordAgain;

    @NotEmpty(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码长度为6位！")
    @Schema(description = "验证码")
    private String verifyCode;

    @Schema(description = "用户头像")
    private String icon;

    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "备注")
    private String remark;
}
