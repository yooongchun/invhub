package zoz.cool.apihub.delegate;


import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import zoz.cool.apihub.config.BaiduOcrConfig;
import zoz.cool.apihub.enums.HttpCode;
import zoz.cool.apihub.vo.BaiduOcrVo;

import java.util.HashMap;
import java.util.Map;

/**
 * 获取数据
 */

@Component
@Slf4j
public class BaiduOcrDelegate {
    @Resource
    private BaiduOcrConfig baiduOcrConfig;

    private String getAccessToken() {
        String uri = String.format(baiduOcrConfig.getUrlPattern(), baiduOcrConfig.getApiAuth(), baiduOcrConfig.getAppId(), baiduOcrConfig.getAppSecret());
        try (HttpResponse response = HttpRequest.get(uri).execute()) {
            if (response.getStatus() != HttpCode.SUCCESS.getCode()) {
                log.error("获取accessToken失败, body={}", response.body());
                return null;
            }
            JSON data = JSONUtil.parse(response.body());
            return data.getByPath("access_token").toString();
        }
    }

    public BaiduOcrVo getOcrData(String imgBase64) {
        String accessToken = getAccessToken();
        Assert.notNull(accessToken, "获取accessToken失败");

        String uri = baiduOcrConfig.getApiOcr() + "?access_token=" + accessToken;
        Map<String, Object> payload = new HashMap<>();
        payload.put("image", imgBase64);
        try (HttpResponse resp = HttpRequest.post(uri)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .form(payload)
                .execute()) {
            Assert.isTrue(resp.getStatus() == HttpCode.SUCCESS.getCode(), "请求失败" + resp.body());
            JSON jsonData = JSONUtil.parse(resp.body());
            Object result = jsonData.getByPath("words_result");
            Assert.isTrue(result != null, "没有words_result字段：" + jsonData);
            BaiduOcrVo baiduOcrVo = JSONUtil.toBean((JSONObject) result, BaiduOcrVo.class);
            log.info("baiduOcrVo: {}", baiduOcrVo);
            return baiduOcrVo;
        }
    }
}