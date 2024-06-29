package zoz.cool.apihub.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VerifyCodeVo {
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(name = "手机号")
    private String phone;
    @Email
    @Schema(name = "邮箱")
    private String email;
    @NotEmpty(message = "验证码key不能为空")
    private String captchaKey;
    @NotEmpty(message = "验证码不能为空")
    private String captchaCode;
}
