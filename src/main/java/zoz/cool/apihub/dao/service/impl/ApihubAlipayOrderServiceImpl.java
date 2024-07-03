package zoz.cool.apihub.dao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import zoz.cool.apihub.dao.domain.ApihubAlipayOrder;
import zoz.cool.apihub.dao.service.ApihubAlipayOrderService;
import zoz.cool.apihub.dao.mapper.ApihubAlipayOrderMapper;
import org.springframework.stereotype.Service;


/**
 * @author yczha
 * @description 针对表【apihub_alipay_order(订单表)】的数据库操作Service实现
 * @createDate 2024-06-29 18:16:04
 */
@Service
public class ApihubAlipayOrderServiceImpl extends ServiceImpl<ApihubAlipayOrderMapper, ApihubAlipayOrder>
        implements ApihubAlipayOrderService {

    public ApihubAlipayOrder getByOrderId(String orderId) {
        return getOne(new QueryWrapper<ApihubAlipayOrder>().eq("order_id", orderId));
    }

    public Page<ApihubAlipayOrder> listByUserId(Long userId, Integer page, Integer pageSize) {
        Page<ApihubAlipayOrder> pageData = new Page<>(page, pageSize);
        pageData = baseMapper.selectPage(pageData, new QueryWrapper<ApihubAlipayOrder>().eq("user_id", userId));
        return pageData;
    }
}
