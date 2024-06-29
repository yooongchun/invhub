package zoz.cool.apihub.dao.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 用户交易记录表
 * @TableName apihub_transaction_record
 */
@TableName(value ="apihub_transaction_record")
@Data
public class ApihubTransactionRecord implements Serializable {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 交易ID
     */
    private String transactionId;

    /**
     * 交易金额
     */
    private BigDecimal transactionAmount;

    /**
     * 交易类型
     */
    private String transactionType;

    /**
     * 交易时间
     */
    private LocalDateTime transactionTime;

    /**
     * 交易状态:SUCCEED-成功，FAILED-失败，CANCELED-取消
     */
    private String transactionStatus;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}