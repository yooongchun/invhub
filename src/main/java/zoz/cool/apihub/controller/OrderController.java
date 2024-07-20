package zoz.cool.apihub.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import zoz.cool.apihub.dao.domain.ApihubAlipayOrder;
import zoz.cool.apihub.dao.domain.ApihubUser;
import zoz.cool.apihub.dao.service.ApihubAlipayOrderService;
import zoz.cool.apihub.dao.service.ApihubUserService;
import zoz.cool.apihub.enums.HttpCode;
import zoz.cool.apihub.exception.ApiException;
import zoz.cool.apihub.service.AlipayOrderService;
import zoz.cool.apihub.service.UserService;
import zoz.cool.apihub.vo.AlipayOrderVo;
import zoz.cool.apihub.vo.OrderVo;

import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@ResponseBody
@Tag(name = "04.订单管理", description = "订单管理")
@RequestMapping("/order")
@SaCheckLogin
public class OrderController {
    @Resource
    private AlipayOrderService alipayOrderService;
    @Resource
    private ApihubAlipayOrderService apihubAlipayOrderService;
    @Resource
    private UserService userService;
    @Resource
    private ApihubUserService apihubUserService;

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
    public Page<OrderVo> orderList(@RequestParam(required = false, defaultValue = "1") Integer page,
                                   @RequestParam(required = false, defaultValue = "10") Integer pageSize,
                                   @RequestParam(required = false) String keywords) {
        Page<ApihubAlipayOrder> pageData = new Page<>(page, pageSize);
        QueryWrapper<ApihubAlipayOrder> q = new QueryWrapper<>();
        q.like(keywords != null, "subject", keywords);
        if (!userService.isAdmin()) {
            q.eq("user_id", StpUtil.getLoginIdAsLong());
        } else if (keywords != null) {
            List<Long> uidList = apihubUserService.list(new QueryWrapper<ApihubUser>().like("username", keywords)).stream().map(ApihubUser::getUid).toList();
            if (!uidList.isEmpty()) {
                q.or().in("user_id", uidList);
            }
        }
        Page<ApihubAlipayOrder> orderPage = apihubAlipayOrderService.page(pageData, q);
        return (Page<OrderVo>) orderPage.convert(order -> {
            OrderVo orderVo = new OrderVo();
            BeanUtils.copyProperties(order, orderVo);
            orderVo.setUsername(apihubUserService.getUserByUid(order.getUserId()).getUsername());
            return orderVo;
        });
    }
}
