package zoz.cool.apihub.enums;

import lombok.Getter;

@Getter
public enum UserSettingEnum {
    AUTO_RUN_INV_CHECK("自动执行查验任务", "1");
    private final String msg;
    private final String value;

    UserSettingEnum(String msg, String value) {
        this.msg = msg;
        this.value = value;
    }

    public static UserSettingEnum getByName(String value) {
        for (UserSettingEnum userSettingEnum : UserSettingEnum.values()) {
            if (userSettingEnum.name().equals(value)) {
                return userSettingEnum;
            }
        }
        return null;
    }
}
