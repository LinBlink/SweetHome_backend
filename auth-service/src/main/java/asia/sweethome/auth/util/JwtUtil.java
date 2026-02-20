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
 * 【JWT 令牌工具】
 * <p>
 * JWT（JSON Web Token）是一段字符串，形如 xxx.yyy.zzz，三段分别是「头.载荷.签名」。
 * 载荷里放着用户 id、手机号、过期时间等信息（是明文 Base64，任何人都能看，所以别放密码等机密）。
 * 关键在于「签名」：本服务用【私钥】签名，别人（如网关）用【公钥】验签。
 * <ul>
 *   <li>私钥只有 auth-service 有 → 只有它能签发合法令牌；</li>
 *   <li>公钥可以公开 → 任何服务都能验证令牌真伪，但无法伪造。</li>
 * </ul>
 * 这套「私钥签名 / 公钥验签」就是 RSA 非对称加密，好处是各业务服务无需知道私钥即可校验登录态。
 *
 * @author: LOCRIAN_V
 * @date: 6/30/2026 8:50 PM
 */
@Component   // 注册为 Spring 组件，别处可注入使用
public class JwtUtil {

    private PrivateKey privateKey;  // 私钥：用于签名（签发令牌）
    private PublicKey publicKey;    // 公钥：用于验签（校验令牌）

    // accessToken 有效期 15 分钟（毫秒）。设短是为了：即使泄漏，很快失效
    private static final long ACCESS_TOKEN_EXPIRE = 15 * 60 * 1000L;
    // refreshToken 有效期 30 天（毫秒）。设长是为了：用户 30 天内不用反复登录
    private static final long REFRESH_TOKEN_EXPIRE = 30L * 24 * 60 * 60 * 1000L;

    // 从配置（application.yml / Nacos）读取 Base64 编码的密钥字符串
    @Value("${jwt.private-key}")
    private String privateKeyStr;

    @Value("${jwt.public-key}")
    private String publicKeyStr;

    /**
     * {@code @PostConstruct}：Bean 创建、依赖注入完成后自动执行一次。
     * 这里把配置里的 Base64 密钥字符串，还原成 Java 能用的 PrivateKey / PublicKey 对象，
     * 只做一次并缓存，避免每次签发令牌都重复解析。
     */
    @PostConstruct
    public void init() throws NoSuchAlgorithmException, InvalidKeySpecException {

        KeyFactory kf = KeyFactory.getInstance("RSA");

        // 私钥采用 PKCS#8 格式：先 Base64 解码成字节，再按格式规范还原成 PrivateKey
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyStr);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        this.privateKey = kf.generatePrivate(spec);

        // 公钥采用 X.509 格式，同理还原成 PublicKey
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyStr);
        X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(publicKeyBytes);
        this.publicKey = kf.generatePublic(publicSpec);

    }

    /** 生成 accessToken（type=access，短期） */
    public String generateAccessToken(Long userId, String phone) {
        return buildToken(userId, phone, ACCESS_TOKEN_EXPIRE, "access");
    }

    /** 生成 refreshToken（type=refresh，长期） */
    public String generateRefreshToken(Long userId, String phone) {
        return buildToken(userId, phone, REFRESH_TOKEN_EXPIRE, "refresh");
    }

    /** 计算 refreshToken 从现在起的到期时刻，用于写进数据库记录 */
    public java.time.LocalDateTime refreshTokenExpiryFromNow() {
        return java.time.LocalDateTime.now().plusSeconds(REFRESH_TOKEN_EXPIRE / 1000);
    }

    /**
     * 真正拼装 JWT 的地方。
     * @param type 令牌类型（access / refresh），放进载荷，续期时用来区分，防止 access 冒充 refresh
     */
    private String buildToken(Long userId, String phone, long expire, String type) {
        Date currentTime = new Date();
        Date expireTime = new Date(currentTime.getTime() + expire);  // 当前时间 + 有效期 = 过期时间

        return Jwts.builder()
                .subject(String.valueOf(userId)) // subject：主体，这里存用户 id
                .claim("phone", phone)           // 自定义载荷：手机号
                .claim("type", type)             // 自定义载荷：令牌类型
                .issuedAt(currentTime)           // 签发时间
                .expiration(expireTime)          // 过期时间（校验时会自动判断）
                .signWith(privateKey)            // 用私钥签名（jjwt 依据 RSA 密钥自动选 RS256 算法）
                .compact();                      // 拼成最终的 xxx.yyy.zzz 字符串
    }

    /**
     * 用公钥验签并解析出载荷内容（Claims）。
     * 若令牌被篡改、签名不符或已过期，jjwt 会抛出异常——调用方据此判定令牌无效。
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)        // 用公钥验签
                .build()
                .parseSignedClaims(token)     // 解析（顺带校验签名与过期时间）
                .getPayload();                // 取出载荷
    }

}
