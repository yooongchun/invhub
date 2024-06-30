package zoz.cool.apihub.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 支付宝支付相关配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "alipay")
public class AlipayConfig {
    /**
     * 支付宝网关
     */
    private String gatewayUrl;
    /**
     * 支付宝分配给开发者的应用ID
     */
    private String appId;
    /**
     * 开发者私钥，由开发者自己生成
     */
    private String alipayPrivateKey;

    /**
     * 支付宝公钥，由支付宝生成。
     */
    private String alipayPublicKey;
    /**
     * 用户确认支付后，支付宝调用的页面返回路径
     */
    private String returnUrl;
    /**
     * 支付宝服务器主动通知商户服务器里的异步通知回调（需要公网能访问）
     */
    private String notifyUrl;
    /**
     * 订单状态轮询最长有效期
     */
    private Integer maxQueryTime;
    /**
     * 参数返回格式，只支持JSON
     */
    private final String format = "JSON";
    /**
     * 请求使用的编码格式
     */
    private final String charset = "UTF-8";
    /**
     * 生成签名字符串所使用的签名算法类型
     */
    private final String signType = "RSA2";
}