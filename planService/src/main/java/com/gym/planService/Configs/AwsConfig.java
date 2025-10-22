package com.gym.planService.Configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AwsConfig {

    private final String AWS_ACCESS_KEY;
    private final String AWS_SECRET_KEY;
    private final String REGION;

    public AwsConfig(
            @Value("${aws.access.key}") String AWS_ACCESS_KEY,
            @Value("${aws.secret.key}") String AWS_SECRET_KEY,
            @Value("${aws.region}") String REGION) {
        this.AWS_ACCESS_KEY = AWS_ACCESS_KEY;
        this.AWS_SECRET_KEY = AWS_SECRET_KEY;
        this.REGION = REGION;
    }

    @Bean
    public S3Client s3Client(){
        AwsBasicCredentials credentials = AwsBasicCredentials
                .create(AWS_ACCESS_KEY,AWS_SECRET_KEY);
        return S3Client.builder()
                .region(Region.of(REGION))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}
