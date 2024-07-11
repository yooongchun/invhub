package zoz.cool.apihub.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class InvInfoVo {
    /**
     * 发票ID
     */
    private Long id;
    /**
     * 发票号
     */
    private String invNum;
    /**
     * 发票代码
     */
    private String invCode;
    /**
     * 税额
     */
    private String tax;
    /**
     * 开票日期
     */
    private LocalDate invDate;
    /**
     * 金额
     */
    private BigDecimal amount;
    /**
     * 校验码
     */
    private String checkCode;
    /**
     * 校验状态
     */
    private Integer checked;
    /**
     * 报销状态
     */
    private Integer reimbursed;
    /**
     * 报销人
     */
    private String owner;
    /**
     * 查验结果:10: 未查验 20: 查验中 30: 成功 31: 查无此票 32: 查验不一致 33: 作废  40: 查验失败
     */
    private Integer invChecked;
}
