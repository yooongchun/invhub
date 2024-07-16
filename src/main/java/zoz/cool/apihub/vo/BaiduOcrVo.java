package zoz.cool.apihub.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class BaiduOcrVo {
    @Schema(description = "发票消费类型。不同消费类型输出：餐饮、电器设备、通讯、服务、日用品食品、医疗、交通、其他")
    private String ServiceType;
    @Schema(description = "发票种类。不同类型发票输出：普通发票、专用发票、电子普通发票、电子专用发票、通行费电子普票、区块链发票、通用机打电子发票、电子发票(专用发票)、电子发票(普通发票)")
    private String InvoiceType;
    @Schema(description = "发票名称")
    private String InvoiceTypeOrg;
    @Schema(description = "发票代码")
    private String InvoiceCode;
    @Schema(description = "发票号码")
    private String InvoiceNum;
    @Schema(description = "发票代码的辅助校验码，一般业务情景可忽略")
    private String InvoiceCodeConfirm;
    @Schema(description = "发票号码的辅助校验码，一般业务情景可忽略")
    private String InvoiceNumConfirm;
    @Schema(description = "数电票号，仅针对纸质的全电发票，在密码区有数电票号码的字段输出")
    private String InvoiceNumDigit;
    @Schema(description = "增值税发票左上角标志。 包含：通行费、销项负数、代开、收购、成品油、其他")
    private String InvoiceTag;
    @Schema(description = "机打号码。仅增值税卷票含有此参数")
    private String MachineNum;
    @Schema(description = "机器编号。仅增值税卷票含有此参数")
    private String MachineCode;
    @Schema(description = "校验码")
    private String CheckCode;
    @Schema(description = "开票日期")
    private String InvoiceDate;
    @Schema(description = "购方名称")
    private String PurchaserName;
    @Schema(description = "购方纳税人识别号")
    private String PurchaserRegisterNum;
    @Schema(description = "购方地址及电话")
    private String PurchaserAddress;
    @Schema(description = "购方开户行及账号")
    private String PurchaserBank;
    @Schema(description = "密码区")
    private String Password;
    @Schema(description = "省")
    private String Province;
    @Schema(description = "市")
    private String City;
    @Schema(description = "联次信息。专票第一联到第三联分别输出：第一联：记账联、第二联：抵扣联、第三联：发票联；普通发票第一联到第二联分别输出：第一联：记账联、第二联：发票联")
    private String SheetNum;
    @Schema(description = "是否代开")
    private String Agent;
    @Schema(description = "货物名称")
    private List<RowWordVo> CommodityName;
    @Schema(description = "规格型号")
    private List<RowWordVo> CommodityType;
    @Schema(description = "单位")
    private List<RowWordVo> CommodityUnit;
    @Schema(description = "数量")
    private List<RowWordVo> CommodityNum;
    @Schema(description = "单价")
    private List<RowWordVo> CommodityPrice;
    @Schema(description = "金额")
    private List<RowWordVo> CommodityAmount;
    @Schema(description = "税率")
    private List<RowWordVo> CommodityTaxRate;
    @Schema(description = "税额")
    private List<RowWordVo> CommodityTax;
    @Schema(description = "车牌号。仅通行费增值税电子普通发票含有此参数")
    private List<RowWordVo> CommodityPlateNum;
    @Schema(description = "类型。仅通行费增值税电子普通发票含有此参数")
    private List<RowWordVo> CommodityVehicleType;
    @Schema(description = "通行日期起。仅通行费增值税电子普通发票含有此参数")
    private List<RowWordVo> CommodityStartDate;
    @Schema(description = "通行日期止。仅通行费增值税电子普通发票含有此参数")
    private List<RowWordVo> CommodityEndDate;
    @Schema(description = "电子支付标识。仅区块链发票含有此参数")
    private String OnlinePay;
    @Schema(description = "销售方名称")
    private String SellerName;
    @Schema(description = "销售方纳税人识别号")
    private String SellerRegisterNum;
    @Schema(description = "销售方地址及电话")
    private String SellerAddress;
    @Schema(description = "销售方开户行及账号")
    private String SellerBank;
    @Schema(description = "合计金额")
    private Integer TotalAmount;
    @Schema(description = "合计税额")
    private Integer TotalTax;
    @Schema(description = "价税合计(大写)")
    private String AmountInWords;
    @Schema(description = "价税合计(小写)")
    private Integer AmountInFiguers;
    @Schema(description = "收款人")
    private String Payee;
    @Schema(description = "复核")
    private String Checker;
    @Schema(description = "开票人")
    private String NoteDrawer;
    @Schema(description = "备注")
    private String Remarks;
    @Schema(description = "判断是否存在印章。返回“0或1”，1代表存在印章，0代表不存在印章，当 seal_tag=true 时返回该字段")
    private String company_seal;
    @Schema(description = "印章识别结果内容。当 seal_tag=true 时返回该字段")
    private String seal_info;
}

