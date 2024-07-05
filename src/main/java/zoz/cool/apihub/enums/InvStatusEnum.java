package zoz.cool.apihub.enums;

import lombok.Getter;

@Getter
public enum InvStatusEnum {
    INIT(0, "初始化"),
    PROCESSING(1, "处理中"),
    SUCCEED(2, "成功"),
    FAILED(-1, "失败");

    private Integer code;
    private String name;

    InvStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
}
