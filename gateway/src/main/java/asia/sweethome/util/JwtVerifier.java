package asia.sweethome.util;

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
 * 【网关的 JWT 验签器（仅公钥）】
 * <p>
 * 网关只需要「验证 token 真伪」，不需要签发，所以只配了公钥。公钥与 auth-service 的私钥配对，
 * 由后者签发的令牌，这里就能验通过；伪造的令牌验不过。原理详见 auth-service 的 JwtUtil。
 *
 * @author: LOCRIAN_V
 * @date: 7/1/2026 8:41 PM
 */
@Component
public class JwtVerifier {

    @Value("${jwt.public-key}")
    private String publicKeyStr;   // 配置里的 Base64 公钥字符串

    private PublicKey publicKey;   // 解析后的公钥对象

    /** Bean 初始化时把公钥字符串还原成 PublicKey，只做一次 */
    @PostConstruct
    public void init() throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory kf = KeyFactory.getInstance("RSA");
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyStr);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        this.publicKey = kf.generatePublic(spec);
    }

    /** 验签并解析出载荷；令牌被篡改或已过期会抛异常，由调用方 catch 后返回 401 */
    public Claims verify(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
