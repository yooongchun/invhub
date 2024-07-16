package zoz.cool.apihub.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import zoz.cool.apihub.dao.domain.ApihubAlipayOrder;
import zoz.cool.apihub.dao.domain.ApihubUser;
import zoz.cool.apihub.dao.service.ApihubAlipayOrderService;
import zoz.cool.apihub.enums.HttpCode;
import zoz.cool.apihub.exception.ApiException;
import zoz.cool.apihub.service.AlipayOrderService;
import zoz.cool.apihub.service.UserService;
import zoz.cool.apihub.vo.AlipayOrderVo;

import java.util.Objects;

@Slf4j
@RestController
@ResponseBody
@Tag(name = "04.订单管理", description = "支付宝订单管理")
@RequestMapping("/alipay/order")
@SaCheckLogin
public class AlipayOrderController {
    @Resource
    private AlipayOrderService alipayOrderService;
    @Resource
    private ApihubAlipayOrderService apihubAlipayOrderService;
    @Resource
    private UserService userService;

    @Operation(summary = "新建订单", description = "新建订单")
    @PostMapping("/")
    public ApihubAlipayOrder create(@Validated @RequestBody AlipayOrderVo alipayOrderVo) {
        // 调用支付宝创建订单
        return alipayOrderService.createOrder(alipayOrderVo);
    }

    @Operation(summary = "查询订单", description = "查询订单")
    @GetMapping("/{orderId}")
    public ApihubAlipayOrder orderInfo(@PathVariable String orderId) {
        ApihubAlipayOrder order = apihubAlipayOrderService.getByOrderId(orderId);
        Assert.notNull(order, "订单不存在");
        ApihubUser user = userService.getLoginUser();
        if (!Objects.equals(user.getUid(), order.getUserId()) && !userService.isAdmin()) {
            throw new ApiException(HttpCode.FORBIDDEN);
        }
        return order;
    }

    @Operation(summary = "按用户查订单", description = "按用户查询订单")
    @GetMapping("/list")
    public Page<ApihubAlipayOrder> orderList(@RequestParam(required = false, defaultValue = "1") Integer page,
                                             @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        return apihubAlipayOrderService.listByUserId(StpUtil.getLoginIdAsLong(), page, pageSize);
    }
}
