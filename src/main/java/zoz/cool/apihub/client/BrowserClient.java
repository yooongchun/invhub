package zoz.cool.apihub.client;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import lombok.extern.slf4j.Slf4j;
import zoz.cool.apihub.dao.domain.ApihubInvInfo;
import zoz.cool.apihub.utils.FileUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

@Slf4j
public class BrowserClient {
    private final ApihubInvInfo invInfo;
    private static final String url = "https://inv-veri.chinatax.gov.cn/index.html";
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
                // 保存验证码图片
                if (autoSave) {
                    saveVerifyImg(base64Img);
                    makeScreenshot(page);
                }
            }
        }
    }

    private void makeScreenshot(Page page) {

        try {
            Path screenshotPath = Paths.get(savePath + "/screenshot");
            if (!screenshotPath.toFile().exists()) {
                if (!screenshotPath.toFile().mkdirs()) {
                    throw new RuntimeException("创建文件夹失败");
                }
            }
            Path path = Paths.get(screenshotPath.toString(), String.valueOf(System.currentTimeMillis()), ".png");
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
            imgSavePath = Paths.get(imgSavePath.toString(), String.valueOf(System.currentTimeMillis()), ".png");
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
        String code = StrUtil.isNotEmpty(invInfo.getCheckCode()) ? invInfo.getCheckCode().substring(invInfo.getCheckCode().length() - 6) : String.valueOf(invInfo.getAmount());
        page.fill("id=kjje", code);
    }

    /**
     * 获取验证码图片
     */
    private static String getVerifyImg(Page page) {
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

    public static void main(String[] args) {
        ApihubInvInfo invInfo = new ApihubInvInfo();
        invInfo.setInvCode("1234567890");
        invInfo.setInvNum("12345678");
        invInfo.setInvDate(LocalDate.now());
        invInfo.setCheckCode("123456");
        invInfo.setAmount(BigDecimal.valueOf(100.0));
        BrowserClient client = new BrowserClient(invInfo, true);
        client.verify();
    }
}
