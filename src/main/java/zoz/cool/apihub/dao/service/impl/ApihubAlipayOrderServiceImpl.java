package zoz.cool.apihub.dao.service.impl;

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
    implements ApihubAlipayOrderService{

}
