package zoz.cool.apihub.vo;

import lombok.Data;
import zoz.cool.apihub.dao.domain.ApihubInvDetail;
import zoz.cool.apihub.enums.InvCheckEnum;

@Data
public class InvCheckInfoVo {
    private InvCheckEnum checkStatus;
    private ApihubInvDetail detail;
    private String reason;
}
