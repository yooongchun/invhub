package zoz.cool.apihub.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
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
import org.springframework.stereotype.Service;
import zoz.cool.apihub.config.AlipayConfig;
import zoz.cool.apihub.dao.domain.ApihubAlipayOrder;
import zoz.cool.apihub.dao.service.ApihubAlipayOrderService;
import zoz.cool.apihub.dao.service.ApihubUserService;
import zoz.cool.apihub.enums.AlipayOrderStatusEnum;
import zoz.cool.apihub.exception.ApiException;
import zoz.cool.apihub.utils.TimeUtil;
import zoz.cool.apihub.utils.ToolKit;
import zoz.cool.apihub.vo.AlipayOrderVo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
    @Resource
    private EmailService emailService;

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
        ApihubAlipayOrder order = makeOrder(alipayOrderVo);
        // 异步查询并更新订单状态
        // TODO: 仅需在开发环境中使用此项，生产环境可依赖于支付宝的异步通知
        asyncUpdateOrder(order.getTradeNo());
        return order;
    }

    private ApihubAlipayOrder makeOrder(AlipayOrderVo alipayOrderVo) {
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
        request.setBizContent(JSONUtil.toJsonStr(bizContent));
        String result;
        String tip = "请求支付宝创建订单失败";
        try {
            result = alipayClient.execute(request).getBody();
            log.info("支付宝创建订单返回结果: {}", result);
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
        // 创建成功， 返回订单对象
        ApihubAlipayOrder alipayOrder = new ApihubAlipayOrder();
        BeanUtils.copyProperties(alipayOrderVo, alipayOrder);
        alipayOrder.setTradeStatus(AlipayOrderStatusEnum.WAIT_BUYER_PAY.name());
        alipayOrder.setQrCode(node.get("qr_code").asText());
        alipayOrder.setUserId(StpUtil.getLoginIdAsLong());
        alipayOrder.setTradeNo(node.get("out_trade_no").asText());
        apihubAlipayOrderService.save(alipayOrder);

        return alipayOrder;
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

    public boolean updateOrder(String outTradeNo) {
        ApihubAlipayOrder order = apihubAlipayOrderService.getByOrderId(outTradeNo);
        Assert.notNull(order, "订单不存在 " + outTradeNo);
        AlipayOrderStatusEnum prevTradeStatus = AlipayOrderStatusEnum.getOrderStatusByName(order.getTradeStatus());
        // 查询状态
        AlipayTradeQueryResponse trade = queryOrder(outTradeNo);
        if (trade == null) {
            log.warn("[AlipayOrder.updateOrder] 查询订单状态失败，outTradeNo:{}", outTradeNo);
            return false;
        }
        // 用户还未扫码，此时提示订单不存在
        if (trade.getSubCode() != null && trade.getSubCode().equals("ACQ.TRADE_NOT_EXIST")) {
            log.warn("[AlipayOrder.updateOrder] 等待用户扫码, response.body: {}", trade.getBody());
            return false;
        }
        AlipayOrderStatusEnum currStatus = AlipayOrderStatusEnum.getOrderStatusByName(trade.getTradeStatus());
        log.info("[AlipayOrder.updateOrder] 当前订单状态: {}", currStatus);
        switch (currStatus) {
            case TRADE_CLOSED, TRADE_FINISHED, TRADE_SUCCESS -> {
                // 交易完成，停止查询
                order.setTradeStatus(currStatus.name());
                order.setBuyerId(trade.getBuyerUserId());
                order.setTradeNo(trade.getTradeNo());
                order.setGmtPayment(TimeUtil.dateToLocalDateTime(trade.getSendPayDate()));
                apihubAlipayOrderService.updateById(order);
                if (currStatus.equals(AlipayOrderStatusEnum.TRADE_SUCCESS)) {
                    // 交易成功，则需要更新账户余额
                    BigDecimal balance = accountService.addBalance(order.getUserId(), order.getAmount());
                    emailService.notifyOrderPayment(order);
                    log.info("[AlipayOrder.updateOrder] 交易成功，更新账户，amount: {}, after: {}", order.getAmount(), balance);
                }
                log.info("[AlipayOrder.updateOrder] 交易完成，currStatus: {}", currStatus);
                return true;
            }
            default -> {
                if (!currStatus.equals(prevTradeStatus)) {
                    log.info("[AlipayOrder.updateOrder] 更新订单状态：{} --> {}", prevTradeStatus, currStatus);
                    order.setTradeStatus(currStatus.name());
                    apihubAlipayOrderService.updateById(order);
                }
                return false;
            }
        }
    }

    public void asyncUpdateOrder(String outTradeNo) {
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(alipayConfig.getMaxQueryTime());
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {
            if (LocalDateTime.now().isAfter(expireTime)) { //如已超时，则退出
                ApihubAlipayOrder order = apihubAlipayOrderService.getByOrderId(outTradeNo);
                order.setTradeStatus(AlipayOrderStatusEnum.TRADE_CLOSED.name());
                apihubAlipayOrderService.updateById(order);
                executorService.shutdown();
            }
            // 否则查询并更新状态
            if (updateOrder(outTradeNo)) {
                executorService.shutdown();
            }
        }, 0, 5, TimeUnit.SECONDS);
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

        request.setBizContent(JSONUtil.toJsonStr(bizContent));
        try {
            return alipayClient.execute(request);
        } catch (AlipayApiException e) {
            log.error("查询支付宝账单异常！", e);
            return null;
        }
    }
}
