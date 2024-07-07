package zoz.cool.apihub.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import zoz.cool.apihub.vo.DailyWordsVo;

/**
 * 文件管理
 */
@Slf4j
@SaCheckLogin
@RestController
@ResponseBody
@RequestMapping("/common")
@Tag(name = "08.通用接口")
public class CommonController {
    private static final String API_SHANBEI = "https://apiv3.shanbay.com/weapps/dailyquote/quote";

    @GetMapping("/daily-words")
    @Operation(summary = "每日一句")
    public DailyWordsVo handleDailyWords() {
        String res = HttpUtil.get(API_SHANBEI);
        log.info("每日一句接口返回：{}", res);
        JSON json = JSONUtil.parse(res);
        DailyWordsVo dailyWordsVo = new DailyWordsVo();
        dailyWordsVo.setContent(json.getByPath("content").toString());
        dailyWordsVo.setTranslation(json.getByPath("translation").toString());
        return dailyWordsVo;
    }
}
