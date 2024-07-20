package zoz.cool.apihub.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import zoz.cool.apihub.dao.domain.ApihubTransactionRecord;
import zoz.cool.apihub.dao.domain.ApihubUser;
import zoz.cool.apihub.dao.service.ApihubTransactionRecordService;
import zoz.cool.apihub.dao.service.ApihubUserService;
import zoz.cool.apihub.service.UserService;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@ResponseBody
@Tag(name = "11.交易管理")
@RequestMapping("/transaction")
@SaCheckLogin
public class TransactionController {
    @Resource
    private UserService userService;
    @Resource
    private ApihubTransactionRecordService apihubTransactionRecordService;
    @Resource
    private ApihubUserService apihubUserService;

    @Operation(summary = "交易列表")
    @GetMapping("/list")
    public Page<ApihubTransactionRecord> getPageTransaction(@RequestParam(required = false, defaultValue = "1") Integer pageNum,
                                                            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
                                                            @RequestParam(required = false) String keywords,
                                                            @RequestParam(required = false) LocalDateTime startTime,
                                                            @RequestParam(required = false) LocalDateTime endTime) {
        Page<ApihubTransactionRecord> pageData = new Page<>(pageNum, pageSize);
        QueryWrapper<ApihubTransactionRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.gt(startTime != null, "create_time", startTime);
        queryWrapper.lt(endTime != null, "create_time", endTime);
        if (!userService.isAdmin()) {
            queryWrapper.eq("user_id", userService.getLoginUser().getUid());
        }
        if (keywords != null) {
            queryWrapper.like("remark", keywords);
            if (userService.isAdmin()) {
                List<Long> uidList = apihubUserService.list(new QueryWrapper<ApihubUser>().like("username", keywords)).stream().map(ApihubUser::getUid).toList();
                if (!uidList.isEmpty()) {
                    queryWrapper.or().in("user_id", uidList);
                }
            }
        }
        return apihubTransactionRecordService.page(pageData, queryWrapper);
    }
}
