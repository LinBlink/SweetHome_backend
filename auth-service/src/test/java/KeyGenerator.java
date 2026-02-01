import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/1/2026 6:00 PM
 */

public class KeyGenerator {

    @Test
    public void genKey() throws NoSuchAlgorithmException {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair kp = gen.generateKeyPair();

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
