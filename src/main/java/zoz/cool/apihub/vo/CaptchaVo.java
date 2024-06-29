package zoz.cool.apihub.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CaptchaVo {
    @Schema(name = "验证码key")
    private String key;
    @Schema(name = "验证码图片")
    private String image;

    public CaptchaVo(String key, String image) {
        this.key = key;
        this.image = image;
    }
}