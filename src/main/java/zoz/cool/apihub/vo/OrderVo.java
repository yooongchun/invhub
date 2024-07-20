package zoz.cool.apihub.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import zoz.cool.apihub.dao.domain.ApihubAlipayOrder;

@EqualsAndHashCode(callSuper = true)
@Data
public class OrderVo extends ApihubAlipayOrder {
    private String username;
}
