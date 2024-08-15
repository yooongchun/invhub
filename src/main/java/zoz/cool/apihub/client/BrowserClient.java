package zoz.cool.apihub.client;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import lombok.extern.slf4j.Slf4j;
import zoz.cool.apihub.dao.domain.ApihubInvDetail;
import zoz.cool.apihub.dao.domain.ApihubInvInfo;
import zoz.cool.apihub.utils.FileUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class BrowserClient {
    private final ApihubInvInfo invInfo;
    private static final String url = "http://inv-veri.chinatax.gov.cn/index.html";
    private static final String detectUrl = "http://43.143.247.21:8000/pred";
    private static final String savePath = "output";
    private static boolean autoSave = false;

    public BrowserClient(ApihubInvInfo invInfo) {
        this.invInfo = invInfo;
    }

    public BrowserClient(ApihubInvInfo invInfo, boolean autoSave) {
        this.invInfo = invInfo;
        BrowserClient.autoSave = autoSave;
    }

    public void verify() {
        try (Playwright playwright = Playwright.create()) {
            BrowserType.LaunchOptions options = new BrowserType.LaunchOptions().setHeadless(false);
            try (Browser browser = playwright.chromium().launch(options)) {
                // 获取一个浏览器实例（自动导航到目标页面）
                Browser.NewContextOptions contextOptions = new Browser.NewContextOptions().setIgnoreHTTPSErrors(true);
                BrowserContext context = browser.newContext(contextOptions);
                Page page = context.newPage();
                page.navigate(url);
                page.waitForLoadState(LoadState.NETWORKIDLE); // 等待页面加载完成

                // 填充基础信息
                fillBaseInvInfo(page);
                // 获取验证码图片
                String base64Img = getVerifyImg(page);
                // 识别验证码
                tryDetectCode(page, base64Img);
                // 保存验证码图片
                if (autoSave) {
                    saveVerifyImg(base64Img);
                    makeScreenshot(page);
                }
                try {
                    Thread.sleep(100000);
                } catch (Exception ignored) {
                }
            }
        }
    }

    private void detectCode(Page page, int times) {
        // 多次尝试
    }

    private void tryDetectCode(Page page, String base64Img) {
        try {
            String tip = page.locator("id=yzminfo").innerText();
            String code = detectVerifyCode(base64Img, tip);
            if (StrUtil.isNotEmpty(code)) {
                // 填写验证码
                page.fill("id=yzm", code);
                // 随便点击一下位置，不然按钮无法点击
                page.locator("id=fpdm").click();
                // 点击查验
                page.locator("id=checkfp").click();
                // 是否成功（如果等待获取到了该元素则成功）
                page.getByText("发票查验明细");
                // 获取详情
                getDetail(page);
            } else {
                log.warn("Detect code empty");
            }
        } catch (Exception e) {
            log.error("Detect code failed", e);
        }
    }

    private ApihubInvDetail getDetail(Page page) {
        // 获取详情
        Map<String, String> invMap = new HashMap<>() {{
            put("fpcc", "发票标题");
            put("fpdm", "发票代码");
            put("fphm", "发票号码");
            put("kprq", "开票日期");
            put("jym", "校验码");
            put("sbbh", "机器编号");
            put("jqbh", "机器编号");
            put("gfmc", "购方名称");
            put("gfsbh", "购方纳税人识别号");
            put("gfdzdh", "购方地址电话");
            put("gfyhzh", "购方开户行及账户");
            put("xfmc", "销方名称");
            put("xfsbh", "销方纳税人识别号");
            put("xfdzdh", "销方地址电话");
            put("xfyhzh", "销方开户行及账户");
            put("bz", "备注");
            put("jshjdx", "价税合计大写");
            put("jshjxx", "价税合计小写");
            put("je", "合计金额");
            put("se", "合计税额");
        }};
        // 获取id后缀
        String suffix = "dzfp";
        Map<String, String> detail = new HashMap<>();
        for (Map.Entry<String, String> entry : invMap.entrySet()) {
            String key = String.format("%s_%s", entry.getKey(), suffix);
            String value = page.locator(key).innerText();
            detail.put(entry.getValue(), value);
        }
        return null;
        /*
        html = await frame.content()

        for id_key, inv_key in inv_map.items():
            try:
                element = await frame.querySelector(f'#{id_key}_{suffix}')
                inv_val = await frame.evaluate('(element) => element.textContent', element)
                inv_key = inv_map[id_key]
                inv_data[inv_key] = inv_val
            except Exception as e:
                logging.error(f'get inv keyword error: #{id_key}_{suffix}，{inv_key}: {e}')
        try:
            elements_key = await frame.querySelectorAll(f'#tab_head_{suffix} > td')
            elements_val = await frame.querySelectorAll(f'#tab_head_{suffix} ~ tr > td')
            rows = (len(elements_val) - 3) / len(elements_key) - 1
            logging.info(
                f'elements_key:{len(elements_key)}\telements_val:{len(elements_val)}\trows:{rows}')
            elements_key_iter = [elements_key[int(i % len(elements_key))]
                                 for i in range(int(rows * len(elements_key)))]
            logging.info(f'\telements_key_iter:{len(elements_key_iter)}')
            for ele_key, ele_val in zip(elements_key_iter, elements_val):
                inv_key = await frame.evaluate('(element) => element.textContent', ele_key)
                inv_val = await frame.evaluate('(element) => element.textContent', ele_val)
                if inv_key not in inv_data:
                    inv_data[inv_key] = inv_val
                else:
                    inv_data[inv_key] = str(inv_data[inv_key]) + ';\n' + str(inv_val)
            return inv_data
        except Exception as e:
            logging.error(f'get inv detail error: {e}')
            return {}
        * */
    }

    private void makeScreenshot(Page page) {

        try {
            Path screenshotPath = Paths.get(savePath + "/screenshot");
            if (!screenshotPath.toFile().exists()) {
                if (!screenshotPath.toFile().mkdirs()) {
                    throw new RuntimeException("创建文件夹失败");
                }
            }
            Path path = Paths.get(screenshotPath.toString(), System.currentTimeMillis() + ".png");
            page.screenshot(new Page.ScreenshotOptions().setPath(path));
            log.info("Save screenshot to: {}", path);
        } catch (Exception e) {
            log.error("Save screenshot failed", e);
        }
    }

    private void saveVerifyImg(String base64Image) {
        try {
            Path imgSavePath = Paths.get(savePath + "/images");
            if (!imgSavePath.toFile().exists()) {
                if (!imgSavePath.toFile().mkdirs()) {
                    throw new RuntimeException("创建文件夹失败");
                }
            }

            BufferedImage yzmImg = FileUtil.base64ToImage(base64Image);
            imgSavePath = Paths.get(imgSavePath.toString(), System.currentTimeMillis() + ".png");
            ImageIO.write(yzmImg, "png", imgSavePath.toFile());
            log.info("Save image to: {}", imgSavePath);
        } catch (IOException e) {
            log.error("Save image failed", e);
        }
    }

    /**
     * 填充基本信息
     */
    private void fillBaseInvInfo(Page page) {
        log.info("Fill base inv info");
        page.fill("id=fpdm", invInfo.getInvCode());
        page.fill("id=fphm", invInfo.getInvNum());
        page.fill("id=kprq", DateUtil.format(invInfo.getInvDate().atStartOfDay(), "yyyyMMdd"));
        page.getByText("发票代码").click(); // 这里需要点击一下，否则下面的输入框无法输入
        String code = StrUtil.isNotEmpty(invInfo.getCheckCode()) ? invInfo.getCheckCode().substring(invInfo.getCheckCode().length() - 6) : String.valueOf(invInfo.getAmount());
        page.fill("id=kjje", code);
    }

    /**
     * 获取验证码图片
     */
    private String getVerifyImg(Page page) {
        page.click("id=yzm_img");
        String defaultYzmStr = "images/code.png";
        for (int i = 0; i < 5; i++) {
            Locator yzmImg = page.locator("id=yzm_img");
            String base64Str = yzmImg.getAttribute("src");
            if (!defaultYzmStr.equals(base64Str)) {
                log.info("Get yzm image success");
                return base64Str;
            }
            try {
                // 等待1s
                log.warn("Waiting yzm image loading {}", i + 1);
                Thread.sleep(1000);
            } catch (Exception ignored) {
            }
        }
        throw new RuntimeException("get yzm image failed");
    }

    private String detectVerifyCode(String base64Img, String tip) throws IOException {
        BufferedImage img = FileUtil.base64ToImage(base64Img);
        // Convert BufferedImage to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        byte[] imageBytes = baos.toByteArray();
        String channel = parseChannel(tip);
        String url = String.format("%s?channel=%s", detectUrl, channel);
        try (HttpResponse response = HttpRequest.post(url).form("file", imageBytes, "image.png").execute()) {
            if (response.isOk()) {
                String res = response.body();
                JSONArray jsonArray = JSONUtil.parseArray(res);
                JSONArray array = (JSONArray) jsonArray.getFirst();
                String code = array.getFirst().toString();
                String ci = array.get(1).toString();
                log.info("tip={} ==> channel={}, code={}, ci={}", tip, channel, code, ci);
                return code;
            }
        }
        return null;
    }

    private String parseChannel(String tip) {
        if (tip.contains("蓝色")) {
            return "blue";
        } else if (tip.contains("红色")) {
            return "red";
        } else if (tip.contains("黄色")) {
            return "yellow";
        } else if (tip.contains("请输入验证码文字")) {
            return "black";
        } else {
            throw new RuntimeException("Invalid tip: " + tip);
        }
    }

    public static void main(String[] args) {
        ApihubInvInfo invInfo = new ApihubInvInfo();
        invInfo.setInvCode("044001629111");
        invInfo.setInvNum("64504308");
        invInfo.setInvDate(DateUtil.parseLocalDateTime("20230105", "yyyyMMdd").toLocalDate());
        invInfo.setCheckCode("314480");
        log.info("invInfo={}", invInfo);
        BrowserClient client = new BrowserClient(invInfo, true);
        client.verify();
    }
}
