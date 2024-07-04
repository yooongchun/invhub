package zoz.cool.apihub.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import zoz.cool.apihub.dao.domain.ApihubInvoiceInfo;

@EqualsAndHashCode(callSuper = true)
@Data
public class InvInfoVo extends ApihubInvoiceInfo {
    private BaiduOcrVo invDetailVo;
}
