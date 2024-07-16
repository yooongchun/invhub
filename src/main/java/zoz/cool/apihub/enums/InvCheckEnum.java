package zoz.cool.apihub.enums;

import lombok.Getter;

@Getter
public enum InvCheckEnum {
    UNCHECKED(10, "未查验"),
    CHECKING(20, "查验中"),
    SUCCESS(30, "已查验"),
    NOT_FOUND(31, "查无此票"),
    INCONSISTENT(32, "不一致"),
    INVALID(33, "已作废"),
    FAILED(40, "查验失败");

    private final Integer code;
    private final String desc;

    InvCheckEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
