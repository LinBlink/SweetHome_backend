import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * 【RSA 密钥对生成器（一次性工具）】
 * <p>
 * 这是一个手动运行的辅助工具（借用 @Test 方便在 IDE 里一键跑），不是自动化测试。
 * 运行一次，把打印出来的：
 * <ul>
 *   <li>PRIVATE KEY → 配置到 auth-service 的 jwt.private-key（用于签发令牌）；</li>
 *   <li>PUBLIC  KEY → 配置到网关 / 各服务的 jwt.public-key（用于验签）。</li>
 * </ul>
 * ⚠️ 私钥属于机密，切勿提交进 Git 或泄漏。
 *
 * @author: LOCRIAN_V
 * @date: 7/1/2026 6:00 PM
 */
public class KeyGenerator {

    @Test
    public void genKey() throws NoSuchAlgorithmException {
        // 1. 选用 RSA 算法，密钥长度 2048 位（安全性与性能的常用平衡点）
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        // 2. 生成一对匹配的公钥 + 私钥
        KeyPair kp = gen.generateKeyPair();

        // 3. 把二进制密钥编码成 Base64 文本，方便粘贴进配置文件
        String privateKey = Base64.getEncoder().encodeToString(
                kp.getPrivate().getEncoded()
        );

        String publicKey = Base64.getEncoder().encodeToString(
                kp.getPublic().getEncoded()
        );

        System.out.println("=== PRIVATE KEY - AUTH ===");
        System.out.println(privateKey);
        System.out.println("=== PUBLIC KEY - GATE ===");
        System.out.println(publicKey);

    }

}
