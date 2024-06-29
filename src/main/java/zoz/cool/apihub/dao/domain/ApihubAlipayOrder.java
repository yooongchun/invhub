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
 * 订单表
 * @TableName apihub_alipay_order
 */
@TableName(value ="apihub_alipay_order")
@Data
public class ApihubAlipayOrder implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 二维码链接
     */
    private String qrCode;

    /**
     * 订单标题/商品标题/交易标题
     */
    private String subject;

    /**
     * 订单总金额
     */
    private BigDecimal amount;

    /**
     * 交易状态
     */
    private String tradeStatus;

    /**
     * 支付宝交易号
     */
    private String tradeNo;

    /**
     * 买家支付宝账号
     */
    private String buyerId;

    /**
     * 交易付款时间
     */
    private LocalDateTime gmtPayment;

    /**
     * 用户在交易中支付的金额
     */
    private BigDecimal buyerPayAmount;

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