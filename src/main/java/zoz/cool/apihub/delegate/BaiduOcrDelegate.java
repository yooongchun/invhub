package zoz.cool.apihub.delegate;


import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import zoz.cool.apihub.config.BaiduOcrConfig;
import zoz.cool.apihub.enums.HttpCode;

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

    private JSON getRawData(String imgBase64) {
        String accessToken = getAccessToken();
        Assert.notNull(accessToken, "获取accessToken失败");

        String uri = baiduOcrConfig.getApiOcr() + "?access_token=" + accessToken;
        LinkedMultiValueMap<String, Object> payload = new LinkedMultiValueMap<>();
        payload.add("image", imgBase64);
        try (HttpResponse resp = HttpRequest.post(uri).body(JSONUtil.toJsonStr(payload)).execute()) {
            Assert.isTrue(resp.getStatus() == HttpCode.SUCCESS.getCode(), "请求失败" + resp.body());
            return JSONUtil.parse(resp.body());
        }
    }
}