package org.aiknowledge.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
@RequiredArgsConstructor
public class R2Config {
    private final AppProperties r2Properties;

    @Bean
    public S3Client r2Client() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                r2Properties.storage().accessKey(),
                r2Properties.storage().secretKey()
        );

        return S3Client.builder()
                .endpointOverride(URI.create(r2Properties.storage().endpoint()))
                .region(Region.of("auto"))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}
