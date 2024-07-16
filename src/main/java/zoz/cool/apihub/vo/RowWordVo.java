package zoz.cool.apihub.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RowWordVo {
    @Schema(description = "行号")
    private Integer row;
    @Schema(description = "内容")
    private String word;
}