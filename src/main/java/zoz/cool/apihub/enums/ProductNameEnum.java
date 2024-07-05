package zoz.cool.apihub.enums;

import lombok.Getter;

@Getter
public enum ProductNameEnum {
    INV_PARSE("发票解析"),
    INV_CHECK("发票查验");
    private final String name;

    ProductNameEnum(String name) {
        this.name = name;
    }
}
