package zoz.cool.apihub.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "验证码请求")
public class CaptchaVo {
    @Schema(description = "验证码key")
    private String captchaKey;
    @Schema(description = "验证码图片")
    private String captchaBase64;

    public CaptchaVo(String key, String image) {
        this.captchaKey = key;
        this.captchaBase64 = image;
    }
}