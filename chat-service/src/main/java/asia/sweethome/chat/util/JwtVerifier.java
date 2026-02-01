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
 * 与 gateway 使用同一把 RSA 公钥验签；用于 WebSocket 握手阶段兜底解析 ?token= 查询参数
 * （正常路径下网关已经验证过并把身份写进 X-User-Id 请求头，这里只是脱离网关直连调试时的兜底）。
 */
@Component
public class JwtVerifier {

    @Value("${jwt.public-key}")
    private String publicKeyStr;

    private PublicKey publicKey;

    @PostConstruct
    public void init() throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory kf = KeyFactory.getInstance("RSA");
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyStr);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        this.publicKey = kf.generatePublic(spec);
    }

    public Claims verify(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
