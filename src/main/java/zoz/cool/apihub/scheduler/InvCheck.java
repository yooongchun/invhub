package zoz.cool.apihub.scheduler;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import zoz.cool.apihub.client.BrowserClient;
import zoz.cool.apihub.config.BrowserConfig;
import zoz.cool.apihub.dao.domain.*;
import zoz.cool.apihub.dao.service.*;
import zoz.cool.apihub.enums.InvCheckEnum;
import zoz.cool.apihub.enums.ProductNameEnum;
import zoz.cool.apihub.enums.UserSettingEnum;
import zoz.cool.apihub.service.EmailService;
import zoz.cool.apihub.service.StorageService;
import zoz.cool.apihub.service.UserService;
import zoz.cool.apihub.vo.InvCheckInfoVo;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.*;
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
    @Resource
    private UserService userService;
    @Resource
    private ApihubProductPriceService productPriceService;
    @Resource
    private ApihubUserService apihubUserService;
    @Resource
    private ApihubUserSettingsService userSettingsService;
    @Resource
    private BrowserConfig browserConfig;


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
        return apihubInvCheckTaskService.list(new QueryWrapper<ApihubInvCheckTask>().eq("status", InvCheckEnum.INIT.getCode()));
    }

    // 异步执行任务
    @Async("inv-check")
    public void checkTask(ApihubInvCheckTask task) {
        // 执行查验
        log.info("开始执行查验任务: taskId={},invId={}", task.getId(), task.getInvId());
        for (int i = 0; i < MAX_RETRY; i++) {
            try {
                // 先将状态记录为处理中
                task.setStatus(InvCheckEnum.PROCESSING.getCode());
                apihubInvCheckTaskService.updateById(task);
                // 随机等待1~15秒
                Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 15000));
                ApihubInvInfo invInfo = invInfoService.getById(task.getInvId());
                BrowserClient client = new BrowserClient(invInfo, false, true, browserConfig.getPath());
                InvCheckInfoVo invCheckInfoVo = client.runCheck();
                if (invCheckInfoVo.getCheckStatus() == InvCheckEnum.FAILED) {
                    // 查验失败，重试
                    log.warn("查验失败，原因：{}", invCheckInfoVo.getReason());
                    continue;
                } else if (invCheckInfoVo.getCheckStatus() != InvCheckEnum.SUCCESS) {
                    // 虽然没有失败，但查验任务并非成功，可能是查无此票、已废弃、超时等
                    log.warn("查验结束但是没有成功，状态 {}", invCheckInfoVo.getCheckStatus().getDesc());
                    task.setStatus(invCheckInfoVo.getCheckStatus().getCode());
                    apihubInvCheckTaskService.updateById(task);
                    return;
                }
                // 否则查验任务成功
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
                task.setStatus(InvCheckEnum.SUCCESS.getCode());
                apihubInvCheckTaskService.updateById(task);
                // 保存结果
                invDetailService.save(invDetail);
                // 记录查验任务ID
                invInfo.setInvCheckId(invDetail.getId());
                invInfoService.updateById(invInfo);
                // 扣费
                BigDecimal price = BigDecimal.valueOf(0.05);
                if (!userService.isAdmin()) {
                    ApihubProductPrice productPrice = productPriceService.getOne(new QueryWrapper<ApihubProductPrice>().eq("product_code", ProductNameEnum.INV_CHECK.name()));
                    if (productPrice == null) {
                        log.error("产品定价不存在");
                    } else {
                        price = productPrice.getPrice();
                    }
                    ApihubUser user = apihubUserService.getUserByUid(invInfo.getUserId());
                    assert user != null;
                    userService.deduceBalance(user, price, ProductNameEnum.INV_CHECK, "查验发票 " + invInfo.getInvCode());
                }
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
        task.setStatus(InvCheckEnum.FAILED.getCode());
        apihubInvCheckTaskService.updateById(task);
    }

    // 自动提交查验任务
    // 10秒执行一次
    @Scheduled(fixedRate = 1000 * 10)
    public void autoSubmitCheckTask() {
        try {
            // 获取用户打开自动查验开关的发票任务
            List<ApihubInvInfo> tasks = getAutoCheckTasks();
            log.info("自动生成查验任务：{}个", tasks.size());
            for (ApihubInvInfo info : tasks) {
                // 仅没有提交过对应任务才提交
                ApihubInvCheckTask task = apihubInvCheckTaskService.getInvCheckTaskByInvIdUid(info.getId(), info.getUserId());
                if (task == null) {
                    // 不存在，提交
                    ApihubInvCheckTask newTask = new ApihubInvCheckTask();
                    newTask.setInvId(info.getId());
                    newTask.setUserId(info.getUserId());
                    newTask.setStatus(InvCheckEnum.INIT.getCode());
                    apihubInvCheckTaskService.save(newTask);
                }
            }
        } catch (Exception e) {
            emailService.notifyInvCheckFailed(String.format("自动生成查验任务异常：%s", e.getMessage()));
            log.error("自动生成查验任务失败", e);
        }
    }

    private List<ApihubInvInfo> getAutoCheckTasks() {
        // 找到打开了自动查验开关的用户，获取对应任务提交查验任务
        List<ApihubUserSettings> settings = userSettingsService.list(new QueryWrapper<ApihubUserSettings>().eq("config_key", UserSettingEnum.AUTO_RUN_INV_CHECK.name()).eq("config_value", UserSettingEnum.AUTO_RUN_INV_CHECK.getValue()));
        List<Long> userIdList = new ArrayList<>();
        for (ApihubUserSettings setting : settings) {
            userIdList.add(setting.getUserId());
        }
        if (!userIdList.isEmpty()) {
            List<ApihubInvInfo> invList = invInfoService.list(new QueryWrapper<ApihubInvInfo>().in("user_id", userIdList));
            log.info("userId={},invInfo={}", userIdList, invList.size());
            return invList;
        }
        return Collections.emptyList();
    }
}