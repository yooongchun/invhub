package zoz.cool.apihub.scheduler;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import zoz.cool.apihub.client.BrowserClient;
import zoz.cool.apihub.dao.domain.ApihubInvCheckTask;
import zoz.cool.apihub.dao.domain.ApihubInvDetail;
import zoz.cool.apihub.dao.domain.ApihubInvInfo;
import zoz.cool.apihub.dao.service.ApihubInvCheckTaskService;
import zoz.cool.apihub.dao.service.ApihubInvDetailService;
import zoz.cool.apihub.dao.service.ApihubInvInfoService;
import zoz.cool.apihub.enums.InvCheckEnum;
import zoz.cool.apihub.enums.InvCheckStatusEnum;
import zoz.cool.apihub.service.EmailService;
import zoz.cool.apihub.service.StorageService;
import zoz.cool.apihub.vo.InvCheckInfoVo;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 定时取表任务，执行查验
 */
@Service
@Slf4j
public class InvCheck {
    @Resource
    private ApihubInvCheckTaskService apihubInvCheckTaskService;
    @Resource
    private EmailService emailService;
    @Resource
    private ApihubInvInfoService invInfoService;
    @Resource
    private StorageService storageService;
    @Resource
    private ApihubInvDetailService invDetailService;


    private final static Integer MAX_RETRY = 5; // 最大重试次数

    // 10秒执行一次
    @Scheduled(fixedRate = 1000 * 10)
    public void launchTask() {
        // 获取新任务提交到线程池
        try {
            List<ApihubInvCheckTask> tasks = getNewTasks();
            log.info("获取到新查验任务：{}个", tasks.size());
            tasks.forEach(this::checkTask);
        } catch (Exception e) {
            emailService.notifyInvCheckFailed(String.format("错误信息：%s", e.getMessage()));
            log.error("发票查验任务执行失败", e);
        }
    }

    // 获取新任务
    private List<ApihubInvCheckTask> getNewTasks() {
        return apihubInvCheckTaskService.list(new QueryWrapper<ApihubInvCheckTask>().eq("status", InvCheckStatusEnum.INIT.getCode()));
    }

    // 异步执行任务
    @Async("inv-check")
    public void checkTask(ApihubInvCheckTask task) {
        // 执行查验
        log.info("开始执行查验任务");
        for (int i = 0; i < MAX_RETRY; i++) {
            try {
                // 先将状态记录为处理中
                task.setStatus(InvCheckStatusEnum.PROCESSING.getCode());
                apihubInvCheckTaskService.updateById(task);
                // 随机等待1~15秒
                Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 15000));
                ApihubInvInfo invInfo = invInfoService.getById(task.getInvId());
                BrowserClient client = new BrowserClient(invInfo, false, true);
                InvCheckInfoVo invCheckInfoVo = client.runCheck();
                if (invCheckInfoVo.getCheckStatus() == InvCheckEnum.FAILED) {
                    // 查验失败，重试
                    log.warn("查验失败，原因：{}", invCheckInfoVo.getReason());
                    continue;
                }
                log.info("Check Result Info={}", invCheckInfoVo);
                ApihubInvDetail invDetail = invCheckInfoVo.getDetail();
                // 结果图片上传s3
                if (invCheckInfoVo.getImage() != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(invCheckInfoVo.getImage(), "png", baos);
                    String savePath = storageService.upload(baos.toByteArray(), "check-result.png");
                    Map<String, String> extra = new HashMap<>();
                    extra.put("invCheckImageS3Path", savePath);
                    invDetail.setExtra(JSONUtil.toJsonStr(extra));
                }
                // 任务更新为成功
                task.setStatus(InvCheckStatusEnum.SUCCESS.getCode());
                apihubInvCheckTaskService.updateById(task);
                // 保存结果
                invDetailService.save(invDetail);
                // 记录查验任务ID
                invInfo.setInvCheckId(invDetail.getId());
                invInfoService.updateById(invInfo);
            } catch (Exception e) {
                log.warn("发票查验任务执行失败，重试 {} 次", i + 1, e);
                try {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(1000 * 10, 1000 * 60));
                } catch (InterruptedException ex) {
                    log.error("线程等待失败", ex);
                }
            }
        }
        // 重试次数用完，任务失败
        task.setStatus(InvCheckStatusEnum.FAIL.getCode());
        apihubInvCheckTaskService.updateById(task);
    }
}