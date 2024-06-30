package zoz.cool.apihub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import zoz.cool.apihub.service.AlipayService;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝订单消息回调
 */
@ResponseBody
@RestController
@Tag(name = "AlipayCallbackController", description = "支付宝订单消息回调")
@RequestMapping("/alipay/callback")
public class AlipayCallbackController {
    @Resource
    private AlipayService alipayService;

    @Operation(summary = "支付宝异步回调", description = "支付宝异步回调")
    @PostMapping("/notify")
    public String notify(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            params.put(name, request.getParameter(name));
        }
        return alipayService.notify(params);
    }
}
