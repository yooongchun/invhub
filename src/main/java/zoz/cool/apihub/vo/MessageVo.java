package zoz.cool.apihub.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import zoz.cool.apihub.dao.domain.ApihubMessage;

@EqualsAndHashCode(callSuper = true)
@Data
public class MessageVo extends ApihubMessage {
    private String username;
    private boolean read;
}
