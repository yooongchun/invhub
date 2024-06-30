package zoz.cool.apihub.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import zoz.cool.apihub.config.AlipayConfig;
import zoz.cool.apihub.dao.domain.ApihubAlipayOrder;
import zoz.cool.apihub.dao.service.ApihubAlipayOrderService;

import java.util.Map;

@Service
@Slf4j
public class AlipayService {
    @Resource
    private AlipayConfig alipayConfig;
    @Resource
    private ApihubAlipayOrderService alipayOrderService;

    public String notify(Map<String, String> params) {
        String result = "failure";
        boolean signVerified;
        try {
            //调用SDK验证签名
            String alipayPublicKey = alipayConfig.getAlipayPublicKey();
            signVerified = AlipaySignature.rsaCheckV1(params, alipayPublicKey, alipayConfig.getCharset(), alipayConfig.getSignType());
        } catch (AlipayApiException e) {
            log.error("支付回调签名校验异常！", e);
            return result;
        }
        if (signVerified) {
            String tradeStatus = params.get("trade_status");
            if ("TRADE_SUCCESS".equals(tradeStatus)) {
                result = "success";
                ApihubAlipayOrder alipayOrder = BeanUtil.fillBeanWithMap(params, new ApihubAlipayOrder(), false);
                alipayOrder.setOrderId(params.get("out_trade_no"));
                log.info("[notify]订单支付成功，alipayOrder:{}", JSONUtil.toJsonStr(alipayOrder));
                // 根据orderId查询订单，并修改订单状态
                ApihubAlipayOrder order = alipayOrderService.getOne(new QueryWrapper<ApihubAlipayOrder>().eq("order_id", alipayOrder.getOrderId()));
                BeanUtils.copyProperties(alipayOrder, order);
                alipayOrderService.updateById(order);
            } else {
                log.warn("[notify]订单未支付成功，trade_status:{}", tradeStatus);
            }
        } else {
            log.warn("支付回调签名校验失败！params:{}", params);
        }
        return result;
    }
}
