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
 * 发票详情表
 * @TableName apihub_inv_detail
 */
@TableName(value ="apihub_inv_detail")
@Data
public class ApihubInvDetail implements Serializable {
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
     * 途径：OCR->ocr，系统解析->auto
     */
    private String method;

    /**
     * 解析状态:0-->初始化，1-->解析中，2-->成功，3-->失败
     */
    private Integer status;

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

    /**
     * 发票消费类型。不同消费类型输出：餐饮、电器设备、通讯、服务、日用品食品、医疗、交通、其他
     */
    private String serviceType;

    /**
     * 发票代码
     */
    private String invoiceCode;

    /**
     * 发票号码
     */
    private String invoiceNum;

    /**
     * 发票种类,不同类型发票输出：普通发票、专用发票、电子普通发票、电子专用发票、通行费电子普票、区块链发票、通用机打电子发票、电子发票(专用发票)、电子发票(普通发票)
     */
    private String invoiceType;

    /**
     * 发票名称
     */
    private String invoiceTypeOrg;

    /**
     * 发票代码的辅助校验码，一般业务情景可忽略
     */
    private String invoiceCodeConfirm;

    /**
     * 发票号码的辅助校验码，一般业务情景可忽略
     */
    private String invoiceNumConfirm;

    /**
     * 数电票号，仅针对纸质的全电发票，在密码区有数电票号码的字段输出
     */
    private String invoiceNumDigit;

    /**
     * 增值税发票左上角标志。 包含：通行费、销项负数、代开、收购、成品油、其他
     */
    private String invoiceTag;

    /**
     * 机打号码。仅增值税卷票含有此参数
     */
    private String machineNum;

    /**
     * 机器编号。仅增值税卷票含有此参数
     */
    private String machineCode;

    /**
     * 校验码
     */
    private String checkCode;

    /**
     * 开票日期
     */
    private String invoiceDate;

    /**
     * 购方名称
     */
    private String purchaserName;

    /**
     * 购方纳税人识别号
     */
    private String purchaserRegisterNum;

    /**
     * 购方地址及电话
     */
    private String purchaserAddress;

    /**
     * 购方开户行及账号
     */
    private String purchaserBank;

    /**
     * 销售方名称
     */
    private String sellerName;

    /**
     * 销售方纳税人识别号
     */
    private String sellerRegisterNum;

    /**
     * 销售方地址及电话
     */
    private String sellerAddress;

    /**
     * 销售方开户行及账号
     */
    private String sellerBank;

    /**
     * 密码区
     */
    private String password;

    /**
     * 省
     */
    private String province;

    /**
     * 市
     */
    private String city;

    /**
     * 联次信息。专票第一联到第三联分别输出：第一联：记账联、第二联：抵扣联、第三联：发票联；普通发票第一联到第二联分别输出：第一联：记账联、第二联：发票联
     */
    private String sheetNum;

    /**
     * 是否代开
     */
    private String agent;

    /**
     * 货物栏，json.string之后存储
     */
    private String commodity;

    /**
     * 合计金额
     */
    private BigDecimal totalAmount;

    /**
     * 合计税额
     */
    private BigDecimal totalTax;

    /**
     * 价税合计(大写)
     */
    private String amountInWords;

    /**
     * 价税合计(小写)
     */
    private BigDecimal amountInFiguers;

    /**
     * 收款人
     */
    private String payee;

    /**
     * 复核
     */
    private String checker;

    /**
     * 开票人
     */
    private String noteDrawer;

    /**
     * 备注
     */
    private String remarks;

    /**
     * 判断是否存在印章。返回“0或1”，1代表存在印章，0代表不存在印章，当 seal_tag=true 时返回该字段
     */
    private String companySeal;

    /**
     * 印章识别结果内容。当 seal_tag=true 时返回该字段
     */
    private String sealInfo;

    /**
     * 额外信息，json.string之后存储
     */
    private String extra;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}