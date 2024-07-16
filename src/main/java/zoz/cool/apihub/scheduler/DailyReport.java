package zoz.cool.apihub.scheduler;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import zoz.cool.apihub.enums.StageEnum;
import zoz.cool.apihub.service.EmailService;
import zoz.cool.apihub.service.StatsDataService;
import zoz.cool.apihub.vo.StatsDataVo;

import java.util.Map;

/**
 * 每日定时发送报告
 */
@Service
@Slf4j
public class DailyReport {
    @Resource
    private EmailService emailService;
    @Resource
    private StatsDataService statsDataService;

    @Scheduled(cron = "0 0 8 * * *")
    public void sendDailyReport() {
        Map<StageEnum, StatsDataVo> dataVo = statsDataService.getStatsData();
        // 发送报表
        emailService.sendDailyReport(dataVo.get(StageEnum.DAY), dataVo.get(StageEnum.WEEK), dataVo.get(StageEnum.MONTH), dataVo.get(StageEnum.YEAR), dataVo.get(StageEnum.ALL));
    }
}
