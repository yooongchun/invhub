package zoz.cool.apihub.enums;


/**
 * Created by zhayongchun on 2023/11/29.
 */
public enum AlipayOrderStatusEnum {
    WAIT_BUYER_PAY(1, "交易创建，等待买家付款"),
    TRADE_CLOSED(2, "未付款交易超时关闭，或支付完成后全额退款"),
    TRADE_SUCCESS(3, "交易支付成功"),
    TRADE_FINISHED(4, "交易结束，不可退款");


    AlipayOrderStatusEnum(int code, String desc) {
    }

    public static AlipayOrderStatusEnum getOrderStatusByName(String name) {
        for (AlipayOrderStatusEnum status : AlipayOrderStatusEnum.values()) {
            if (status.name().equals(name)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No AlipayOrderStatusEnum for name " + name);
    }
}