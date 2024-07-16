package zoz.cool.apihub.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StatsDataVo {
    @Schema(description = "用户数量")
    private Integer userNum;
    @Schema(description = "订单数量")
    private Integer orderNum;
    @Schema(description = "订单总数(含未成功订单)")
    private Integer orderTotalNum;
    @Schema(description = "消费笔数")
    private Integer consumeNum;
    @Schema(description = "IP数量")
    private Integer ipNum;
    @Schema(description = "充值金额")
    private BigDecimal orderAmount;
    @Schema(description = "消费金额")
    private BigDecimal consumeAmount;
}
