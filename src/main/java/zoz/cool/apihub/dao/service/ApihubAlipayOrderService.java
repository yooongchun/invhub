package zoz.cool.apihub.dao.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import zoz.cool.apihub.dao.domain.ApihubAlipayOrder;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author yczha
 * @description 针对表【apihub_alipay_order(订单表)】的数据库操作Service
 * @createDate 2024-06-29 18:16:04
 */
public interface ApihubAlipayOrderService extends IService<ApihubAlipayOrder> {

    ApihubAlipayOrder getByOrderId(String orderId);

    Page<ApihubAlipayOrder> listByUserId(Long userId, Integer page, Integer pageSize);
}
