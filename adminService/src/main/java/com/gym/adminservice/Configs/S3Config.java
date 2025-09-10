package com.gym.adminservice.Configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration

/*
 * This class is used to configure the S3 client
 * It uses the AWS SDK for Java to create an S3 client
 * It uses the access key, secret key and region from the application.properties
 * file
 * It returns an S3 client bean that can be used to interact with the S3 service
 */
public class S3Config {

    // Injecting values from application.properties file for AWS access key
    @Value("${aws.access.key}")
    private final String accessKey;

    // Injecting values from application.properties file for AWS secret key
    @Value("${aws.secret.key}")
    private final String secretKey;

    // Injecting values from application.properties file for AWS region
    @Value("${aws.region}")
    private final String region;

    public S3Config( @Value("${aws.access.key}") String accessKey,
                     @Value("${aws.secret.key}")  String secretKey,
                     @Value("${aws.region}") String region) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.region = region;
    }

    /*
     * This method creates and returns an S3 client bean
     * It uses the access key, secret key and region to create the client
     */
    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey); // Creating AWS credentials
                                                                                            // using access key and
                                                                                            // secret key
        return S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials)) // Setting the credentials provider
                                                                                    // using the created credentials by
                                                                                    // StaticCredentialsProvider
                .region(Region.of(region))
                .build();
    }
}
