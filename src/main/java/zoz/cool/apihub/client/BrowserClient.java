package zoz.cool.apihub.client;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.microsoft.playwright.*;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.options.LoadState;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import zoz.cool.apihub.dao.domain.ApihubInvDetail;
import zoz.cool.apihub.dao.domain.ApihubInvInfo;
import zoz.cool.apihub.enums.InvCheckEnum;
import zoz.cool.apihub.utils.FileUtil;
import zoz.cool.apihub.vo.InvCheckInfoVo;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Slf4j
public class BrowserClient {
    @Resource
    private final ApihubInvInfo invInfo;
    private static final String url = "http://inv-veri.chinatax.gov.cn/index.html";
    private static final String detectUrl = "http://43.143.247.21:8000/pred";
    private static final String savePath = "output";
    private static boolean autoSave = false;
    private static boolean headless = false;
    private static final int MAX_TRY_VERIFY_CODE = 5; // 验证码错误重试次数
    private static final int MAX_TRY_QUERY_CODE = 3; // 请求识别验证码接口重试次数
    private static final int MAX_TRY_WAIT_YZM_IMG = 5; // 获取验证码图片（点击刷新）最大重试次数
    private static final int MAX_TRY_WAIT_FRAME = 5; // 等待frame加载最大重试次数
    private static final String dialogBody = "#dialog-body"; // frame弹窗的id

    public BrowserClient(ApihubInvInfo invInfo) {
        this.invInfo = invInfo;
    }

    public BrowserClient(ApihubInvInfo invInfo, boolean autoSave, boolean headless) {
        this.invInfo = invInfo;
        BrowserClient.autoSave = autoSave;
        BrowserClient.headless = headless;
    }

    public BrowserClient(ApihubInvInfo invInfo, boolean autoSave) {
        this.invInfo = invInfo;
        BrowserClient.autoSave = autoSave;
    }

    public InvCheckInfoVo runCheck() {
        InvCheckInfoVo vo = new InvCheckInfoVo();
        try (Playwright playwright = Playwright.create()) {
            BrowserType.LaunchOptions options = new BrowserType.LaunchOptions().setHeadless(headless).setArgs(List.of("--disable-infobars", "--start-maximized", "--disable-dev-shm-usage", "--no-sandbox", "--disable-gpu", "--disable-extensions", "--disable-blink-features=AutomationControlled", "--disable-web-security", "--disable-features=IsolateOrigins,site-per-process"));
            try (Browser browser = playwright.chromium().launch(options)) {
                // 获取一个浏览器实例（自动导航到目标页面）
                Browser.NewContextOptions contextOptions = new Browser.NewContextOptions().setIgnoreHTTPSErrors(true).setLocale("zh-CN").setViewportSize(1920, 1080).setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36");
                BrowserContext context = browser.newContext(contextOptions);
                Page page = context.newPage();
                page.evaluate("() =>{ Object.defineProperties(navigator,{ webdriver:{ get: () => false } }) }");
                page.navigate(url);
                page.waitForLoadState(LoadState.NETWORKIDLE); // 等待页面加载完成
                // 填充基础信息
                String reason = fillBaseInvInfo(page);
                if (reason != null) {
                    vo.setCheckStatus(InvCheckEnum.FAILED);
                    vo.setReason(reason);
                    makeScreenshot(page);
                    return vo;
                }

                // 识别验证码
                if (!tryDetectCode(page, 0)) {
                    // 失败了，返回
                    vo.setCheckStatus(InvCheckEnum.FAILED);
                    vo.setReason("验证码识别失败");
                    makeScreenshot(page);
                    return vo;
                }

                // 开始获取字段
                Frame dialogFrame = null;
                for (int i = 0; i < MAX_TRY_WAIT_FRAME; i++) {
                    if (checkLimited(page)) {
                        // 访问被限制
                        log.error("Access limited");
                        vo.setCheckStatus(InvCheckEnum.ACCESS_LIMITED);
                        makeScreenshot(page);
                        return vo;
                    }
                    if (checkExceedTimes(page)) {
                        // 超过查验次数
                        vo.setCheckStatus(InvCheckEnum.EXCEED_TIMES);
                        makeScreenshot(page);
                        return vo;
                    }
                    if (checkConsistent(page)) {
                        // 查验结果不一致
                        vo.setCheckStatus(InvCheckEnum.INCONSISTENT);
                        makeScreenshot(page);
                        return vo;
                    }
                    if (checkNotFound(page)) {
                        // 查无此票
                        vo.setCheckStatus(InvCheckEnum.NOT_FOUND);
                        makeScreenshot(page);
                        return vo;
                    }
                    if (checkInValid(page)) {
                        // 已作废
                        vo.setCheckStatus(InvCheckEnum.INVALID);
                        makeScreenshot(page);
                        return vo;
                    }
                    dialogFrame = page.frame(dialogBody.substring(1));
                    if (dialogFrame != null) {
                        break;
                    }
                    log.info("wait frame loading... {}/{}", i + 1, MAX_TRY_WAIT_FRAME);
                    sleep(1000);
                }
                if (dialogFrame == null) {
                    vo.setCheckStatus(InvCheckEnum.FAILED);
                    vo.setReason("frame为null");
                    makeScreenshot(page);
                    return vo;
                }
                try {
                    InvCheckInfoVo invCheckInfoVo = getDetail(dialogFrame);
                    invCheckInfoVo.setImage(getCaptureWithCrop(page));
                    return invCheckInfoVo;
                } catch (Exception e) {
                    log.error("parse detail failed", e);
                    makeScreenshot(page);
                    vo.setCheckStatus(InvCheckEnum.FAILED);
                    return vo;
                }
            }
        }
    }

    private boolean tryDetectCode(Page page, int depth) {
        // 递归深度，达到最大重试次数退出
        if (depth >= MAX_TRY_VERIFY_CODE) {
            log.warn("reach max try detect code depth, exit!");
            return false;
        }
        // 尝试填写识别验证码
        try {
            // 获取验证码图片
            sleep(1000);
            String base64Img = getVerifyImg(page);
            // 保存验证码图片
            if (autoSave) {
                saveVerifyImg(base64Img);
            }
            // 获取提示信息
            String tip = page.locator("id=yzminfo").innerText();
            String code = detectVerifyCode(base64Img, tip);
            if (StrUtil.isEmpty(code)) {
                return false;
            }
            // 填写验证码
            page.fill("id=yzm", code);
            // 随便点击一下位置，不然按钮无法点击
            sleep(200, 1000);
            page.locator("id=fpdm").click();
            // 判断是否可点击
            assertThat(page.locator("id=checkfp")).isEnabled();
            makeScreenshot(page);
            // 点击查验
            page.locator("id=checkfp").click();
            sleep(1000);
            if (checkVerifyCodeErr(page)) {
                page.locator("id=popup_ok").click();
                throw new RuntimeException("verify code not correct, try again!");
            }
            return true;
        } catch (Exception e) {
            log.error("tryDetectCode failed, times={}", depth, e);
            sleep(200, 1000);
            return tryDetectCode(page, depth + 1);
        }
    }

    private boolean checkExceedTimes(Page page) {
        // 超过该张发票当日查验次数(请于次日再次查验)
        return page.getByText("超过该张发票当日查验次数(请于次日再次查验)").isVisible();
    }

    private boolean checkConsistent(Page page) {
        // 查验结果不一致
        Locator locator = page.frameLocator(dialogBody).locator("#cyjg");
        return locator.isVisible() && Objects.equals(locator.innerText(), "不一致");
    }

    private boolean checkNotFound(Page page) {
        // 查无此票
        Locator locator = page.frameLocator(dialogBody).locator("#cyjg");
        return locator.isVisible() && Objects.equals(locator.innerText(), "查无此票");
    }

    private boolean checkInValid(Page page) {
        // 已作废
        Locator locator = page.frameLocator(dialogBody).locator("#cyjg");
        return locator.isVisible() && Objects.equals(locator.innerText(), "已作废");
    }

    private boolean checkVerifyCodeErr(Page page) {
        // 验证码是否正确
        return page.getByText("验证码错误").isVisible();
    }

    private boolean checkLimited(Page page) {
        // 是否被判定为异常行为
        return page.frameLocator(dialogBody).getByText("云安全平台检测到您当前的访问行为存在异常").isVisible();
    }

    private InvCheckInfoVo getDetail(Frame frame) {
        InvCheckInfoVo vo = new InvCheckInfoVo();
        vo.setDetail(new ApihubInvDetail());
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
        for (Map.Entry<String, String> entry : invMap.entrySet()) {
            String key = String.format("#%s_%s", entry.getKey(), suffix);
            try {
                if (!frame.locator(key).isVisible()) {
                    log.warn("key={} not visible", key);
                    continue;
                }
                String value = frame.locator(key).innerText();
                setKey(entry.getValue(), value, vo.getDetail());
            } catch (Exception e) {
                log.error("parse detail failed", e);
                vo.setCheckStatus(InvCheckEnum.FAILED);
                return vo;
            }
        }
        try {
            Map<String, List<String>> detail = new HashMap<>();
            List<ElementHandle> keys = frame.querySelectorAll("#tab_head_" + suffix + " > td");
            List<ElementHandle> vals = frame.querySelectorAll("#tab_head_" + suffix + " ~ tr > td");
            if (!keys.isEmpty()) {
                int rows = (vals.size() - 3) / keys.size() - 1;
                log.info("keys={},vals={},rows={}", keys.size(), vals.size(), rows);
                for (int i = 0; i < rows * keys.size(); i++) {
                    ElementHandle eleKey = keys.get(i % keys.size());
                    ElementHandle eleVal = vals.get(i);
                    String invKey = eleKey.textContent();
                    String invVal = eleVal.textContent();
                    if (!detail.containsKey(invKey)) {
                        detail.put(invKey, new ArrayList<>(List.of(invVal)));
                    } else {
                        detail.get(invKey).add(invVal);
                    }
                }
                // 货物栏
                vo.getDetail().setCommodity(JSONUtil.toJsonStr(detail));
                log.info("details={}", detail);
            }
        } catch (Exception e) {
            log.error("parse detail failed", e);
            vo.setCheckStatus(InvCheckEnum.FAILED);
            return vo;
        }
        vo.setCheckStatus(InvCheckEnum.SUCCESS);
        return vo;
    }

    private void setKey(String invKey, String invVal, ApihubInvDetail detail) {
        switch (invKey) {
            case "发票标题" -> detail.setInvoiceTypeOrg(invVal);
            case "发票代码" -> detail.setInvoiceCode(invVal);
            case "发票号码" -> detail.setInvoiceNum(invVal);
            case "开票日期" -> detail.setInvoiceDate(invVal);
            case "校验码" -> detail.setCheckCode(invVal);
            case "机器编号" -> detail.setMachineCode(invVal);
            case "购方名称" -> detail.setPurchaserName(invVal);
            case "购方纳税人识别号" -> detail.setPurchaserRegisterNum(invVal);
            case "购方地址电话" -> detail.setPurchaserAddress(invVal);
            case "购方开户行及账户" -> detail.setPurchaserBank(invVal);
            case "销方名称" -> detail.setSellerName(invVal);
            case "销方纳税人识别号" -> detail.setSellerRegisterNum(invVal);
            case "销方地址电话" -> detail.setSellerAddress(invVal);
            case "销方开户行及账户" -> detail.setSellerBank(invVal);
            case "备注" -> detail.setExtra(invVal);
            case "价税合计大写" -> detail.setAmountInWords(invVal);
            case "价税合计小写" -> detail.setAmountInFiguers(parseAmount(invVal));
            case "合计金额" -> detail.setTotalAmount(parseAmount(invVal));
            case "合计税额" -> detail.setTotalTax(parseAmount(invVal));
        }
    }

    private BigDecimal parseAmount(String invVal) {
        return BigDecimal.valueOf(Double.parseDouble(invVal.replace("￥", "")));
    }

    private void makeScreenshot(Page page) {
        if (!autoSave) {
            return;
        }
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

    private BufferedImage getCaptureWithCrop(Page page) {
        try {
            Path tempPath = Paths.get("temp_screenshot.png");
            page.screenshot(new Page.ScreenshotOptions().setPath(tempPath));
            BufferedImage originalImage = ImageIO.read(tempPath.toFile());
            int cropX = 400;
            int cropY = 200;
            int cropWidth = 1100;
            int cropHeight = 880;
            BufferedImage croppedImage = new BufferedImage(cropWidth, cropHeight, originalImage.getType());
            Graphics2D g = croppedImage.createGraphics();
            g.drawImage(originalImage, 0, 0, cropWidth, cropHeight, cropX, cropY, cropX + cropWidth, cropY + cropHeight, null);
            g.dispose();

            // Delete the temporary file
            tempPath.toFile().delete();

            // Return the cropped BufferedImage
            return croppedImage;
        } catch (IOException e) {
            log.error("Capture and crop screenshot failed", e);
            return null;
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
    private String fillBaseInvInfo(Page page) {
        log.info("Fill base inv info");
        page.fill("id=fpdm", invInfo.getInvCode());
        focusDismiss(page);
        sleep(50, 100);
        if (page.getByText("发票代码有误").isVisible()) {
            return page.locator("#fpdmjy").innerText();
        }
        page.fill("id=fphm", invInfo.getInvNum());
        focusDismiss(page);
        sleep(50, 100);
        if (page.getByText("发票号码有误").isVisible()) {
            return page.locator("#fphmjy").innerText();
        }
        page.fill("id=kprq", DateUtil.format(invInfo.getInvDate().atStartOfDay(), "yyyyMMdd"));
        focusDismiss(page);
        sleep(50, 100);
        String code = StrUtil.isNotEmpty(invInfo.getCheckCode()) ? invInfo.getCheckCode().substring(invInfo.getCheckCode().length() - 6) : String.valueOf(invInfo.getAmount());
        page.fill("id=kjje", code);
        focusDismiss(page);
        return null;
    }

    /**
     * 获取验证码图片
     */
    private String getVerifyImg(Page page) {
        Locator yzmImg = page.locator("id=yzm_img");
        String prevImgStr = yzmImg.getAttribute("src");
        page.click("id=yzm_img");
        String defaultYzmStr = "images/code.png";
        for (int i = 0; i < MAX_TRY_WAIT_YZM_IMG; i++) {
            String base64Str = yzmImg.getAttribute("src");
            if (!defaultYzmStr.equals(base64Str) && getHashCode(prevImgStr) != getHashCode(base64Str)) {
                log.info("Get yzm image success");
                return base64Str;
            }
            prevImgStr = base64Str;
            try {
                // 等待1s
                log.warn("Waiting yzm image loading {}", i + 1);
                Thread.sleep(1000);
            } catch (Exception ignored) {
            }
        }
        throw new RuntimeException("get yzm image failed");
    }


    private void focusDismiss(Page page) {
        // 点击一个地方，用于输入框失焦，触发自动校验过程
        page.getByText("发票号码").first().click();
    }

    private String detectVerifyCode(String base64Img, String tip) throws IOException {
        BufferedImage img = FileUtil.base64ToImage(base64Img);
        // Convert BufferedImage to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        byte[] imageBytes = baos.toByteArray();
        String channel = parseChannel(tip);
        String url = String.format("%s?channel=%s", detectUrl, channel);
        log.info("Detect code, tip={}, channel={},url={}", tip, channel, url);
        for (int i = 0; i < MAX_TRY_QUERY_CODE; i++) { // 重试3次
            try (HttpResponse response = HttpRequest.post(url).form("file", imageBytes, "image.png").execute()) {
                assert response.isOk();
                String responseBody = response.body();
                log.info("responseBody={}", responseBody);
                JSONObject rawBody = JSONUtil.parseObj(responseBody);
                Integer code = rawBody.get("code", Integer.class);
                assert code != null && code == 0;
                JSONObject data = rawBody.getJSONObject("data");
                String predLabel = data.get("pred_label", String.class);
                String predConfidence = data.get("pred_ci", String.class);
                log.info("channel={}, predLabel={}, predConfidence={}", channel, predLabel, predConfidence);
                return predLabel;
            } catch (Exception e) {
                log.error("request failed, try times {}", i + 1, e);
                sleep(1000);
            }
        }
        return null;
    }

    private void sleep(long minMills, long maxMills) {
        try {
            long mills = RandomUtil.randomLong(minMills, maxMills);
            log.info("Sleep {}ms", mills);
            Thread.sleep(mills);
        } catch (InterruptedException e) {
            log.error("sleep error", e);
        }
    }

    private int getHashCode(String str) {
        return str.hashCode();
    }

    private void sleep(long mills) {
        sleep(mills, mills + 1);
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

    public static void main(String[] args) throws IOException {
//        System.setProperty("http.proxyHost", "http://127.0.0.1:8000");
//        System.setProperty("https.proxyHost", "http://127.0.0.1:8000");

        ApihubInvInfo invInfo = new ApihubInvInfo();
        invInfo.setInvCode("044002207111");
        invInfo.setInvNum("88214881");
        invInfo.setInvDate(DateUtil.parseLocalDateTime("20240602", "yyyyMMdd").toLocalDate());
        invInfo.setAmount(BigDecimal.valueOf(172.02+22.37));
        invInfo.setCheckCode("040577");
        log.info("invInfo={}", invInfo);
        BrowserClient client = new BrowserClient(invInfo, true, false);
        InvCheckInfoVo invCheckInfoVo = client.runCheck();
        log.info("Check Result Info={}", invCheckInfoVo);
        File file = Paths.get("output/screenshot/test.png").toFile();
        file.mkdirs();
        ImageIO.write(invCheckInfoVo.getImage(), "png", file);
    }
}
