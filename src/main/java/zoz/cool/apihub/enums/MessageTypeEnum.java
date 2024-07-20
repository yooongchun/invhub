package zoz.cool.apihub.enums;

import lombok.Getter;

@Getter
public enum MessageTypeEnum {
    USER(0, "用户消息"),
    SYSTEM(1, "系统消息");

    private Integer code;
    private String message;

    MessageTypeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static MessageTypeEnum getByCode(Integer code) {
        for (MessageTypeEnum value : MessageTypeEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("未知的消息类型");
    }
}
