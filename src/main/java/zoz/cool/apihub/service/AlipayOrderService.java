package zoz.cool.apihub.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import zoz.cool.apihub.config.AlipayConfig;
import zoz.cool.apihub.dao.domain.ApihubAlipayOrder;
import zoz.cool.apihub.dao.service.ApihubAlipayOrderService;
import zoz.cool.apihub.dao.service.ApihubUserService;
import zoz.cool.apihub.enums.AlipayOrderStatus;
import zoz.cool.apihub.exception.ApiException;
import zoz.cool.apihub.utils.TimeUtil;
import zoz.cool.apihub.utils.ToolKit;
import zoz.cool.apihub.vo.AlipayOrderVo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
public class AlipayOrderService {
    @Resource
    private ApihubAlipayOrderService apihubAlipayOrderService;
    @Resource
    private AlipayClient alipayClient;
    private static final ObjectMapper mapper;
    @Resource
    private AlipayConfig alipayConfig;
    @Resource
    private ApihubUserService accountService;

    static {
        mapper = Jackson2ObjectMapperBuilder.json().simpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .featuresToEnable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature())
                .build();
    }

    public ApihubAlipayOrder createOrder(AlipayOrderVo alipayOrderVo) {
        // 调用支付宝创建订单
        String orderId = ToolKit.genOrderId();
        alipayOrderVo.setOrderId(orderId);
        String qrCode = makeOrder(alipayOrderVo);
        // 创建成功
        ApihubAlipayOrder alipayOrder = new ApihubAlipayOrder();
        BeanUtils.copyProperties(alipayOrderVo, alipayOrder);
        alipayOrder.setTradeStatus(AlipayOrderStatus.WAIT_BUYER_PAY.name());
        alipayOrder.setQrCode(qrCode);
        alipayOrder.setUserId(StpUtil.getLoginIdAsLong());
        apihubAlipayOrderService.save(alipayOrder);
        // 异步查询并更新订单状态
        // TODO: 仅需在开发环境中使用此项，生产环境可依赖于支付宝的异步通知
        updateOrder(alipayOrder.getOrderId());
        return alipayOrder;
    }

    private String makeOrder(AlipayOrderVo alipayOrderVo) {
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        if (StrUtil.isNotEmpty(alipayConfig.getNotifyUrl())) {
            //异步接收地址，公网可访问
            request.setNotifyUrl(alipayConfig.getNotifyUrl());
        }
        if (StrUtil.isNotEmpty(alipayConfig.getReturnUrl())) {
            //同步跳转地址
            request.setReturnUrl(alipayConfig.getReturnUrl());
        }
        // ******必传参数******
        JSONObject bizContent = getBizContent(alipayOrderVo);
        request.setBizContent(bizContent.toString());
        String result;
        String tip = "请求支付宝创建订单失败";
        try {
            result = alipayClient.execute(request).getBody();
        } catch (AlipayApiException e) {
            log.error(tip, e);
            throw new ApiException(tip);
        }
        JsonNode rootNode;
        try {
            rootNode = mapper.readValue(result, JsonNode.class);
        } catch (JsonProcessingException e) {
            log.error(tip, e);
            throw new ApiException(tip);
        }
        JsonNode node = rootNode.get("alipay_trade_precreate_response");
        if (!node.get("code").asText().equals("10000")) {
            log.error("{}, body: {}", tip, result);
            throw new ApiException(tip);
        }
        return node.get("qr_code").asText();
    }

    @NotNull
    private static JSONObject getBizContent(AlipayOrderVo alipayOrderParam) {
        JSONObject bizContent = new JSONObject();
        //商户订单号，商家自定义，保持唯一性
        bizContent.put("out_trade_no", alipayOrderParam.getOrderId());
        //支付金额，最小值0.01元
        bizContent.put("total_amount", alipayOrderParam.getAmount());
        //订单标题，不可使用特殊符号
        bizContent.put("subject", alipayOrderParam.getSubject());
        bizContent.put("scene", "bar_code");
        return bizContent;

    }

    @Async
    public void updateOrder(String outTradeNo) {
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(alipayConfig.getMaxQueryTime());
        AlipayOrderStatus prevTradeStatus = AlipayOrderStatus.WAIT_BUYER_PAY;
        while (true) {
            // 是否超时
            if (LocalDateTime.now().isAfter(expireTime)) {
                log.warn("[AlipayOrder.updateOrder] 支付超时，tradeStatus:{}", prevTradeStatus);
                return;
            }
            // 查询状态
            AlipayTradeQueryResponse trade = queryOrder(outTradeNo);
            if (trade == null) {
                return;
            }
            // 支付失败
            if (!trade.isSuccess()) {
                log.warn("[AlipayOrder.updateOrder] 支付失败, response.body: {}", trade.getBody());
                return;
            }
            AlipayOrderStatus status = AlipayOrderStatus.getOrderStatusByName(trade.getTradeStatus());
            if (status.equals(prevTradeStatus)) {
                log.info("[AlipayOrder.updateOrder] 订单状态未改变，tradeStatus:{}", trade.getTradeStatus());
                return;
            }
            log.info("[AlipayOrder.updateOrder] 更新订单状态，tradeStatus:{}-->{}", prevTradeStatus, status);
            prevTradeStatus = status;
            // 将支付宝返回的订单状态更新到数据库
            ApihubAlipayOrder order = apihubAlipayOrderService.getByOrderId(outTradeNo);
            order.setTradeStatus(status.name());
            order.setBuyerId(trade.getBuyerUserId());
            order.setBuyerPayAmount(new BigDecimal(trade.getBuyerPayAmount()));
            order.setTradeNo(trade.getTradeNo());
            order.setGmtPayment(TimeUtil.dateToLocalDateTime(trade.getSendPayDate()));
            apihubAlipayOrderService.updateById(order);
            // 订单完成，则需要更新账户余额
            if (status.equals(AlipayOrderStatus.TRADE_SUCCESS)) {
                BigDecimal balance = accountService.addBalance(order.getUserId(), order.getAmount());
                log.info("[AlipayOrder.updateOrder] 更新账户，account:{}-->{}", order.getAmount(), balance);
                return;
            }
            if (status.equals(AlipayOrderStatus.TRADE_FINISHED)) {
                log.info("[AlipayOrder.updateOrder] 订单完成，tradeStatus: {}", status);
                return;
            }
            // 否则，休眠5秒后再次查询
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                log.error("[AlipayOrder.AsyncQuery] 线程休眠异常", e);
                break;
            }
        }
    }

    private AlipayTradeQueryResponse queryOrder(String outTradeNo) {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        //******必传参数******
        JSONObject bizContent = new JSONObject();
        //设置查询参数，out_trade_no和trade_no至少传一个
        bizContent.put("out_trade_no", outTradeNo);
        //交易结算信息: trade_settle_info
        String[] queryOptions = {"trade_settle_info"};
        bizContent.put("query_options", queryOptions);

        request.setBizContent(bizContent.toString());
        try {
            return alipayClient.execute(request);
        } catch (AlipayApiException e) {
            log.error("查询支付宝账单异常！", e);
            return null;
        }
    }
}
