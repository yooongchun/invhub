package zoz.cool.apihub.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import zoz.cool.apihub.service.UserService;

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

    public void statsData() {
        userService.checkAdmin();
    }
}
