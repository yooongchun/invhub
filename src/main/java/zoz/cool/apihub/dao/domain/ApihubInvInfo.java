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
 * @TableName apihub_inv_info
 */
@TableName(value ="apihub_inv_info")
@Data
public class ApihubInvInfo implements Serializable {
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
     * 解析任务ID
     */
    private Long invDetailId;

    /**
     * 查验任务ID
     */
    private Long invCheckId;

    /**
     * 是否已人工校验,0-->否，1-->是
     */
    private Integer checked;

    /**
     * 是否已报销：0->未报销；1->已报销；2->在途；3->已驳回
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
    private String checkCode;

    /**
     * 开票日期
     */
    private LocalDate invDate;

    /**
     * 开具金额
     */
    private BigDecimal amount;

    /**
     * 税额
     */
    private String tax;

    /**
     * 发票类型:增值税专用发票:->01、增值税电子专用发票->02、增值税普通发票->03、增值税电子普通发票->04、增值税普通发票(卷票)->05、增值税电子普通发票(通行费)->06，未知->99
     */
    private String invType;

    /**
     * 途径：手动上传->manual，系统解析->auto
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