package zoz.cool.apihub.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import zoz.cool.apihub.dao.domain.ApihubAlipayOrder;
import zoz.cool.apihub.dao.domain.ApihubTransactionRecord;
import zoz.cool.apihub.dao.domain.ApihubUser;
import zoz.cool.apihub.dao.service.ApihubAlipayOrderService;
import zoz.cool.apihub.dao.service.ApihubTransactionRecordService;
import zoz.cool.apihub.enums.AlipayOrderStatusEnum;
import zoz.cool.apihub.enums.StageEnum;
import zoz.cool.apihub.enums.TransactionStatusEnum;
import zoz.cool.apihub.service.StatsDataService;
import zoz.cool.apihub.service.UserService;
import zoz.cool.apihub.vo.MetaInfoVo;
import zoz.cool.apihub.vo.StatsDataVo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 统计数据
 */
@Slf4j
@SaCheckLogin
@RestController
@ResponseBody
@RequestMapping("/stats")
@Tag(name = "09.统计数据")
public class StatsDataController {
    @Resource
    private UserService userService;
    @Resource
    private ApihubAlipayOrderService apihubAlipayOrderService;
    @Resource
    private ApihubTransactionRecordService transactionRecordService;
    @Resource
    private StatsDataService statsDataService;

    @GetMapping("/data")
    @Operation(summary = "统计数据")
    public Map<StageEnum, StatsDataVo> getStatsData() {
        userService.checkAdmin();
        return statsDataService.getStatsData();
    }

    @GetMapping("/meta")
    @Operation(summary = "用户账单元数据")
    public MetaInfoVo getMetaData() {
        ApihubUser user = userService.getLoginUser();

        MetaInfoVo metaInfoVo = new MetaInfoVo();
        // 余额
        metaInfoVo.setBalanceAmount(user.getBalance());
        // 充值金额
        List<ApihubAlipayOrder> orders = apihubAlipayOrderService.list(new QueryWrapper<ApihubAlipayOrder>().eq("user_id", user.getUid()).eq("trade_status", AlipayOrderStatusEnum.TRADE_SUCCESS.name()));
        orders.stream().map(ApihubAlipayOrder::getAmount).reduce(BigDecimal::add).ifPresent(metaInfoVo::setOrderAmount);
        // 消费金额
        List<ApihubTransactionRecord> transactions = transactionRecordService.list(new QueryWrapper<ApihubTransactionRecord>().eq("user_id", user.getUid()).eq("transaction_status", TransactionStatusEnum.SUCCEED.name()));
        transactions.stream().map(ApihubTransactionRecord::getTransactionAmount).reduce(BigDecimal::add).ifPresent(metaInfoVo::setConsumeAmount);
        return metaInfoVo;
    }
}
