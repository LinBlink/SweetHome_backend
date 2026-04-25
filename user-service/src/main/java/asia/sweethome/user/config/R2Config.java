package asia.sweethome.user.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/12/2026 6:11 PM
 */
@Configuration
public class R2Config {

    @Value("${sh.r2.endpoint}")
    private String endpoint;

    @Value("${sh.r2.access-key-id}")
    private String accessKeyId;

    @Value("${sh.r2.secret-access-key}")
    private String secretAccessKey;

    @Bean
    public S3Client r2Client(){
        return S3Client.builder()
                .endpointOverride(
                        URI.create(endpoint)
                )
                .region(
                        Region.of("auto")
                )
                .credentialsProvider(
                        StaticCredentialsProvider
                                .create(
                                        AwsBasicCredentials.create(
                                                accessKeyId,
                                                secretAccessKey
                                        )
                                )
                )
                .serviceConfiguration(
                        S3Configuration.builder()
                                .pathStyleAccessEnabled(true).build()
                ).build();
    }

}



