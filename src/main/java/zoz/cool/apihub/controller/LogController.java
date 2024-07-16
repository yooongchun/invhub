package zoz.cool.apihub.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import zoz.cool.apihub.dao.domain.ApihubLoginLog;
import zoz.cool.apihub.dao.service.ApihubLoginLogService;
import zoz.cool.apihub.dao.service.ApihubUserService;
import zoz.cool.apihub.service.UserService;
import zoz.cool.apihub.vo.LogsVo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户管理
 */
@Slf4j
@RestController
@ResponseBody
@RequestMapping("/logs")
@Tag(name = "07.登录日志接口")
@SaCheckLogin
public class LogController {
    @Resource
    private UserService userService;
    @Resource
    private ApihubUserService apihubUserService;
    @Resource
    private ApihubLoginLogService apihubLoginLogService;

    @Operation(summary = "日志列表")
    @GetMapping("/")
    public Page<LogsVo> listLogs(@RequestParam(defaultValue = "1") Integer pageNum, @RequestParam(defaultValue = "10") Integer pageSize, @RequestParam(required = false, name = "keywords") String kewWords, @RequestParam(required = false) LocalDate startTime, @RequestParam(required = false) LocalDate endTime) {
        userService.checkAdmin();
        Page<ApihubLoginLog> pageLogs = apihubLoginLogService.listLogs(pageNum, pageSize, kewWords, startTime, endTime);
        List<ApihubLoginLog> records = pageLogs.getRecords();
        Page<LogsVo> pageData = new Page<>(pageNum, pageSize);
        pageData.setTotal(pageLogs.getTotal());
        pageData.setPages(pageLogs.getPages());

        List<LogsVo> recordsVo = new ArrayList<>();
        for (ApihubLoginLog record : records) {
            LogsVo logsVo = new LogsVo();
            BeanUtils.copyProperties(record, logsVo);
            logsVo.setOperator(apihubUserService.getUserByUid(record.getUserId()).getUsername());
            logsVo.setLog("用户登录");
            recordsVo.add(logsVo);
        }
        pageData.setRecords(recordsVo);
        return pageData;
    }

}
