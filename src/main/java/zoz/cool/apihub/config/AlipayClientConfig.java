package zoz.cool.apihub.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.Cipher;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * 支付宝请求客户端配置
 */
@Configuration
@Slf4j
public class AlipayClientConfig {

    @Bean
    public AlipayClient alipayClient(AlipayConfig config) {
        try {
            String privateKeyStr = new String(Files.readAllBytes(Paths.get("/Users/zhayongchun/Documents/javis/javis-admin/src/main/resources/alipay/alipayPrivateKey.pem")));
            // 私钥字符串
            privateKeyStr = privateKeyStr.replace("-----BEGIN PRIVATE KEY-----\n", "")
                    .replace("\n-----END PRIVATE KEY-----\n", "")
                    .replaceAll("\n", "");
            log.info("privateKeyString={}", privateKeyStr);
            // 将私钥字符串转换为PrivateKey对象
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyStr);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            // 要加密的内容
            String content = "Hello, world!";

            // 使用私钥加密内容
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            byte[] encryptedBytes = cipher.doFinal(content.getBytes());

            // 将加密后的字节转换为Base64编码的字符串
            String encryptedContent = Base64.getEncoder().encodeToString(encryptedBytes);
            System.out.println("Encrypted content: " + encryptedContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new DefaultAlipayClient(config.getGatewayUrl(), config.getAppId(), config.getAlipayPrivateKey(),
                config.getFormat(), config.getCharset(), config.getAlipayPublicKey(), config.getSignType());
    }
}