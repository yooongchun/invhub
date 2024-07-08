package zoz.cool.apihub.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import zoz.cool.apihub.dao.domain.ApihubAlipayOrder;
import zoz.cool.apihub.dao.domain.ApihubLoginLog;
import zoz.cool.apihub.dao.domain.ApihubTransactionRecord;
import zoz.cool.apihub.dao.domain.ApihubUser;
import zoz.cool.apihub.dao.service.ApihubAlipayOrderService;
import zoz.cool.apihub.dao.service.ApihubLoginLogService;
import zoz.cool.apihub.dao.service.ApihubTransactionRecordService;
import zoz.cool.apihub.dao.service.ApihubUserService;
import zoz.cool.apihub.enums.AlipayOrderStatusEnum;
import zoz.cool.apihub.enums.StageEnum;
import zoz.cool.apihub.vo.StatsDataVo;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计数据
 */
@Service
@Slf4j
public class StatsDataService {
    @Resource
    private EmailService emailService;
    @Resource
    private ApihubUserService apihubUserService;
    @Resource
    private ApihubAlipayOrderService apihubAlipayOrderService;
    @Resource
    private ApihubTransactionRecordService apihubTransactionRecordService;
    @Resource
    private ApihubLoginLogService apihubLoginLogService;

    public Map<StageEnum, StatsDataVo> getStatsData() {

        Map<StageEnum, StatsDataVo> statsDataMap = new HashMap<>();

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        // 获取今天的数据
        LocalDateTime startOfToday = today.atStartOfDay();
        StatsDataVo nowVo = getDataVo(startOfToday, now);
        statsDataMap.put(StageEnum.NOW, nowVo);

        // 获取昨天的数据
        LocalDate yesterday = today.minusDays(1);
        LocalDateTime startOfYesterday = yesterday.atStartOfDay();
        LocalDateTime endOfYesterday = LocalDateTime.of(yesterday, LocalTime.MAX);
        StatsDataVo dayVo = getDataVo(startOfYesterday, endOfYesterday);
        statsDataMap.put(StageEnum.DAY, dayVo);

        // 获取本周的数据
        LocalDateTime startOfMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
        StatsDataVo weekVo = getDataVo(startOfMonday, now);
        statsDataMap.put(StageEnum.WEEK, weekVo);

        // 获取本月的数据
        LocalDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay();
        StatsDataVo monthVo = getDataVo(startOfMonth, now);
        statsDataMap.put(StageEnum.MONTH, monthVo);

        // 获取本年的数据
        LocalDateTime startOfYear = today.withDayOfYear(1).atStartOfDay();
        StatsDataVo yearVo = getDataVo(startOfYear, now);
        statsDataMap.put(StageEnum.YEAR, yearVo);

        // 获取所有数据
        LocalDateTime startOfAll = LocalDateTime.of(2010, 1, 1, 0, 0);
        StatsDataVo allVo = getDataVo(startOfAll, now);
        statsDataMap.put(StageEnum.ALL, allVo);
        return statsDataMap;
    }

    private StatsDataVo getDataVo(LocalDateTime startTime, LocalDateTime endTime) {
        List<ApihubUser> userList = apihubUserService.list(new QueryWrapper<ApihubUser>().between("create_time", startTime, endTime));
        List<ApihubAlipayOrder> orderAllList = apihubAlipayOrderService.list(new QueryWrapper<ApihubAlipayOrder>().between("create_time", startTime, endTime));
        List<ApihubAlipayOrder> orderSuccList = orderAllList.stream().filter(order -> order.getTradeStatus().equals(AlipayOrderStatusEnum.TRADE_SUCCESS.name())).toList();
        List<ApihubTransactionRecord> transactionList = apihubTransactionRecordService.list(new QueryWrapper<ApihubTransactionRecord>().between("transaction_time", startTime, endTime));
        List<ApihubLoginLog> loginLogList = apihubLoginLogService.list(new QueryWrapper<ApihubLoginLog>().between("create_time", startTime, endTime));

        StatsDataVo dataVo = new StatsDataVo();
        dataVo.setUserNum(userList == null ? 0 : userList.size());
        dataVo.setIpNum(loginLogList == null ? 0 : loginLogList.stream().map(ApihubLoginLog::getIp).distinct().toList().size());
        dataVo.setOrderNum(orderSuccList.size());
        dataVo.setOrderTotalNum(orderAllList.size());
        dataVo.setOrderAmount(orderSuccList.stream().map(ApihubAlipayOrder::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        dataVo.setConsumeNum(transactionList == null ? 0 : transactionList.size());
        dataVo.setConsumeAmount(transactionList == null ? BigDecimal.ZERO : transactionList.stream().map(ApihubTransactionRecord::getTransactionAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        return dataVo;
    }
}