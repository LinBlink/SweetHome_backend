package asia.sweethome.auth.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 6/30/2026 8:50 PM
 */

@Component
public class JwtUtil {

    /*
     * 一般
     * 公钥可以用来加密
     * 私钥可以用来解密
     *
     * 数学上
     * 私钥可以签名
     * 公钥可以验证
     * */

    private PrivateKey privateKey;
    private PublicKey publicKey;


    private static final long ACCESS_TOKEN_EXPIRE = 15 * 60 * 1000L;
    private static final long REFRESH_TOKEN_EXPIRE = 30L * 24 * 60 * 60 * 1000L;

    @Value("${jwt.private-key}")
    private String privateKeyStr;

    @Value("${jwt.public-key}")
    private String publicKeyStr;


    @PostConstruct
    public void init() throws NoSuchAlgorithmException, InvalidKeySpecException {

        KeyFactory kf = KeyFactory.getInstance("RSA");

        byte[] keyBytes = Base64.getDecoder().decode(privateKeyStr);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        this.privateKey = kf.generatePrivate(spec);

        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyStr);
        X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(publicKeyBytes);
        this.publicKey = kf.generatePublic(publicSpec);

    }

    public String generateAccessToken(Long userId, String phone) {
        return buildToken(
                userId, phone, ACCESS_TOKEN_EXPIRE, "access"
        );
    }

    public String generateRefreshToken(Long userId, String phone) {
        return buildToken(
                userId, phone, REFRESH_TOKEN_EXPIRE, "refresh"
        );
    }

    public java.time.LocalDateTime refreshTokenExpiryFromNow() {
        return java.time.LocalDateTime.now().plusSeconds(REFRESH_TOKEN_EXPIRE / 1000);
    }

    private String buildToken(
            Long userId, String phone, long expire, String type
    ) {
        Date currentTime = new Date();
        Date expireTime = new Date(currentTime.getTime() + expire);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("phone", phone)
                .claim("type", type)
                .issuedAt(currentTime)
                .expiration(expireTime)
                .signWith(privateKey)
                .compact();

    }

    // 公钥验证，并且得到 消息内部明文
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


}
