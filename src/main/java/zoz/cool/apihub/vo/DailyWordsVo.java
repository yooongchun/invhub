package zoz.cool.apihub.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DailyWordsVo {
    @Schema(description = "内容（英文）")
    private String content;
    @Schema(description = "译文")
    private String translation;
}
