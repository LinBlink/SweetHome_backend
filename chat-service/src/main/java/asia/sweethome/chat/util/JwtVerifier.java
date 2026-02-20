package asia.sweethome.chat.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * 【JWT 验签器（仅公钥）】
 * <p>
 * 只持有公钥、只能「验签」不能「签发」（签发是 auth-service 私钥的事，见 auth 模块的 JwtUtil）。
 * 用于 WebSocket 握手阶段兜底解析 URL 上的 ?token=（正常路径下网关已验过并写入 X-User-Id，
 * 这里只是脱离网关直连调试时的备用方案）。原理见 auth-service 的 JwtUtil 说明。
 */
@Component
public class JwtVerifier {

    @Value("${jwt.public-key}")
    private String publicKeyStr;

    private PublicKey publicKey;

    /** Bean 初始化时把 Base64 公钥字符串还原成 PublicKey 对象，只做一次 */
    @PostConstruct
    public void init() throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory kf = KeyFactory.getInstance("RSA");
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyStr);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        this.publicKey = kf.generatePublic(spec);
    }

    /** 验签并解析出载荷；令牌被篡改/过期会抛异常 */
    public Claims verify(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
