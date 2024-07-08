package zoz.cool.apihub.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MetaInfoVo {
    @Schema(description = "充值金额")
    private BigDecimal orderAmount;
    @Schema(description = "消费金额")
    private BigDecimal consumeAmount;
    @Schema(description = "余额")
    private BigDecimal balanceAmount;

}
