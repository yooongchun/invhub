package zoz.cool.apihub.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import zoz.cool.apihub.dao.domain.ApihubAlipayOrder;
import zoz.cool.apihub.dao.domain.ApihubTransactionRecord;
import zoz.cool.apihub.dao.domain.ApihubUser;
import zoz.cool.apihub.dao.service.ApihubAlipayOrderService;
import zoz.cool.apihub.dao.service.ApihubTransactionRecordService;
import zoz.cool.apihub.dao.service.ApihubUserService;
import zoz.cool.apihub.service.EmailService;
import zoz.cool.apihub.utils.TimeUtil;
import zoz.cool.apihub.vo.ReportDataVo;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/**
 * 每日定时发送报告
 */
@Service
@Slf4j
public class DailyReport {
    @Resource
    private EmailService emailService;
    @Resource
    private ApihubUserService apihubUserService;
    @Resource
    private ApihubAlipayOrderService apihubAlipayOrderService;
    @Resource
    private ApihubTransactionRecordService apihubTransactionRecordService;

    @Scheduled(cron = "0 0 8 * * *")
    public void sendDailyReport() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        // 获取昨天的数据
        LocalDate yesterday = today.minusDays(1);
        LocalDateTime startOfYesterday = yesterday.atStartOfDay();
        LocalDateTime endOfYesterday = LocalDateTime.of(yesterday, LocalTime.MAX);
        ReportDataVo dayVo = getDataVo(startOfYesterday, endOfYesterday);
        // 获取本周的数据
        LocalDateTime startOfMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
        ReportDataVo weekVo = getDataVo(startOfMonday, now);
        // 获取本月的数据
        LocalDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay();
        ReportDataVo monthVo = getDataVo(startOfMonth, now);
        // 获取本年的数据
        LocalDateTime startOfYear = today.withDayOfYear(1).atStartOfDay();
        ReportDataVo yearVo = getDataVo(startOfYear, now);
        // 获取所有数据
        LocalDateTime startOfAll = LocalDateTime.of(2010, 1, 1, 0, 0);
        ReportDataVo allVo = getDataVo(startOfAll, now);
        // 发送报表
        emailService.sendDailyReport(dayVo, weekVo, monthVo, yearVo, allVo);
    }

    private ReportDataVo getDataVo(LocalDateTime startTime, LocalDateTime endTime) {
        List<ApihubUser> userList = apihubUserService.list(new QueryWrapper<ApihubUser>().between("create_time", startTime, endTime));
        List<ApihubAlipayOrder> orderList = apihubAlipayOrderService.list(new QueryWrapper<ApihubAlipayOrder>().between("create_time", startTime, endTime));
        List<ApihubTransactionRecord> transactionList = apihubTransactionRecordService.list(new QueryWrapper<ApihubTransactionRecord>().between("transaction_time", startTime, endTime));

        ReportDataVo dataVo = new ReportDataVo();
        dataVo.setDate(TimeUtil.getLocalDateFormatted());
        dataVo.setUserNum(userList == null ? 0 : userList.size());
        dataVo.setOrderNum(orderList == null ? 0 : orderList.size());
        dataVo.setOrderAmount(orderList == null ? BigDecimal.ZERO : orderList.stream().map(ApihubAlipayOrder::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        dataVo.setConsumeAmount(transactionList == null ? BigDecimal.ZERO : transactionList.stream().map(ApihubTransactionRecord::getTransactionAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        return dataVo;
    }
}
