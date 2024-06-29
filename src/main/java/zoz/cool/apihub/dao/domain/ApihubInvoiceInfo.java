package zoz.cool.apihub.dao.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 发票信息表
 * @TableName apihub_invoice_info
 */
@TableName(value ="apihub_invoice_info")
@Data
public class ApihubInvoiceInfo implements Serializable {
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
     * 文件ID
     */
    private Long fileId;

    /**
     * 解析状态,0-->初始化，1-->处理中，2-->成功，-1-->失败
     */
    private Integer status;

    /**
     * 是否已人工校验,0-->否，1-->是
     */
    private Integer checked;

    /**
     * 是否已报销：0->未报销；1->已报销
     */
    private Integer reimbursed;

    /**
     * 发票代码
     */
    private String invCode;

    /**
     * 发票号码
     */
    private String invNum;

    /**
     * 校验码
     */
    private String invChk;

    /**
     * 开票日期
     */
    private LocalDate invDate;

    /**
     * 开具金额
     */
    private BigDecimal invMoney;

    /**
     * 税额
     */
    private String invTax;

    /**
     * 价税合计
     */
    private String invTotal;

    /**
     * 发票类型:增值税专用发票、增值税电子专用发票、增值税普通发票、增值税电子普通发票、增值税普通发票(卷票)、增值税电子普通发票(通行费)
     */
    private String invType;

    /**
     * 详细信息
     */
    private String invDetail;

    /**
     * 解析方式
     */
    private String method;

    /**
     * 是否删除:0-->否，1-->是
     */
    private Integer deleted;

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