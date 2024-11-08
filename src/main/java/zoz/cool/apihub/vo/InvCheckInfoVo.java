package zoz.cool.apihub.vo;

import lombok.Data;
import zoz.cool.apihub.dao.domain.ApihubInvDetail;
import zoz.cool.apihub.enums.InvCheckEnum;

import java.awt.image.BufferedImage;

@Data
public class InvCheckInfoVo {
    private InvCheckEnum checkStatus;
    private ApihubInvDetail detail;
    private String reason;
    private BufferedImage image;
}
