package zoz.cool.apihub.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import zoz.cool.apihub.dao.domain.ApihubAlipayOrder;
import zoz.cool.apihub.dao.service.ApihubAlipayOrderService;
import zoz.cool.apihub.service.AlipayOrderService;
import zoz.cool.apihub.utils.ToolKit;
import zoz.cool.apihub.vo.AlipayOrderVo;

@Slf4j
@RestController
@ResponseBody
@Tag(name = "AlipayOrderController", description = "支付宝订单管理")
@RequestMapping("/alipay/order")
@SaCheckLogin
public class AlipayOrderController {

    @Resource
    private AlipayOrderService alipayOrderService;
    @Resource
    private ApihubAlipayOrderService apihubAlipayOrderService;

    @Operation(summary = "新建订单", description = "新建订单")
    @PostMapping("/")
    public ApihubAlipayOrder create(@Validated @RequestBody AlipayOrderVo alipayOrderVo) {
        // 调用支付宝创建订单
        return alipayOrderService.createOrder(alipayOrderVo);
    }

    @Operation(summary = "查询订单", description = "查询订单")
    @GetMapping("/{orderId}")
    public ApihubAlipayOrder orderInfo(@PathVariable String orderId) {
        return apihubAlipayOrderService.getByOrderId(orderId);
    }
}
