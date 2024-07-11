package zoz.cool.apihub.enums;

import lombok.Getter;

@Getter
public enum InvTypeEnum {
    /**
     * 普通发票、专用发票、电子普通发票、电子专用发票、通行费电子普票、区块链发票、通用机打电子发票、电子发票(专用发票)、电子发票(普通发票)
     */
    NORMAL("普通发票", "01"),
    SPECIAL("专用发票", "02"),
    ELEC_NORMAL("电子普通发票", "03"),
    ELEC_SPECIAL("电子专用发票", "04"),
    ELEC_NORMAL_VEHICLE("通行费电子普票", "05"),
    BLOCK_CHAIN("区块链发票", "06"),
    ELEC_GENERAL_MACHINE("通用机打电子发票", "07"),
    ELEC_SPECIAL2("电子发票(专用发票)", "08"),
    ELEC_NORMAL2("电子发票(普通发票)", "09"),
    UNKNOWN("未知", "99");

    private final String name;
    private final String code;

    InvTypeEnum(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public static InvTypeEnum getEnumByCode(String code) {
        for (InvTypeEnum invTypeEnum : InvTypeEnum.values()) {
            if (invTypeEnum.getCode().equals(code)) {
                return invTypeEnum;
            }
        }
        return UNKNOWN;
    }

    public static InvTypeEnum getEnumByName(String name) {
        for (InvTypeEnum invTypeEnum : InvTypeEnum.values()) {
            if (invTypeEnum.getName().equals(name)) {
                return invTypeEnum;
            }
        }
        return UNKNOWN;
    }
}
