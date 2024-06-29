package zoz.cool.apihub.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginVo {
    @Schema(description = "用户名")
    private String username;

    @Pattern(regexp = "^\\w{6,20}$", message = "密码必须为6~20位！")
    @Schema(description = "密码")
    private String password;

    @Pattern(regexp = "^\\d{6}$", message = "验证码长度为6位！")
    @Schema(description = "验证码")
    private String verifyCode;

    @Email
    @Schema(description = "邮箱")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号")
    private String phone;
}