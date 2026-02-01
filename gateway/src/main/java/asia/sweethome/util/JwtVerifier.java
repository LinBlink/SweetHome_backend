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
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/1/2026 8:41 PM
 */

@Component
public class JwtVerifier {

    @Value("${jwt.public-key}")
    private String publicKeyStr;

    private PublicKey publicKey;

    @PostConstruct
    public void init() throws NoSuchAlgorithmException, InvalidKeySpecException {

        KeyFactory kf = KeyFactory.getInstance("RSA");

        byte[] keyBytes = Base64.getDecoder().decode(
                publicKeyStr
        );

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
