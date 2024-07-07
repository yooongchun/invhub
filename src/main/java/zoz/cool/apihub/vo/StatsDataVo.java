package zoz.cool.apihub.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StatsDataVo {
    private Integer userNum;
    private Integer orderNum;
    private Integer consumeNum;
    private Integer ipNum;
    private BigDecimal orderAmount;
    private BigDecimal consumeAmount;
}
