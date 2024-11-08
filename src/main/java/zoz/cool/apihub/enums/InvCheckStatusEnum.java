package zoz.cool.apihub.enums;

import lombok.Getter;

@Getter
public enum InvCheckStatusEnum {
    // 析状态:0-->初始化，1-->解析中，2-->成功，3-->失败
    INIT(0, "初始化"),
    PROCESSING(1, "查验中"),
    SUCCESS(2, "成功"),
    FAIL(3, "失败");
    private final Integer code;
    private final String message;

    InvCheckStatusEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
