package zoz.cool.apihub.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReportDataVo {
    private Integer userNum;
    private Integer orderNum;
    private BigDecimal orderAmount;
    private BigDecimal consumeAmount;
    private String date;
}
