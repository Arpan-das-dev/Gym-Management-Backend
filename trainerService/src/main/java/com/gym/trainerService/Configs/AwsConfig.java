package com.gym.trainerService.Configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
/**
 * AWS Configuration class responsible for creating and managing AWS SDK clients.
 * <p>
 * This configuration defines the {@link S3Client} bean used across the application
 * for AWS S3 operations such as file upload, retrieval, and deletion.
 * </p>
 *
 * <p><b>Best Practices:</b></p>
 * <ul>
 *   <li>Avoid hardcoding AWS credentials; always inject via environment variables or configuration files.</li>
 *   <li>Use IAM roles or AWS Secrets Manager for production deployments to ensure security.</li>
 *   <li>Ensure the region is consistent with your S3 bucket’s location to reduce latency.</li>
 * </ul>
 *
 * <p><b>Example Usage:</b></p>
 * <pre>
 * {@code
 * @Service
 * public class FileStorageService {
 *     private final S3Client s3Client;
 *
 *     public FileStorageService(S3Client s3Client) {
 *         this.s3Client = s3Client;
 *     }
 *
 *     public void uploadFile(...) {
 *         s3Client.putObject(...);
 *     }
 * }
 * }
 * </pre>
 *
 * @author Arpan
 * @since 1.0
 */
@Configuration
public class AwsConfig {
    /** AWS Access Key ID injected from application properties or environment variables. */
    private final String accessKey;

    /** AWS Secret Access Key injected from application properties or environment variables. */
    private final String secretKey;

    /** AWS region (e.g., "ap-south-1") used for the S3 client configuration. */
    private final String region;

    /**
     * Constructs an {@link AwsConfig} instance and injects AWS credentials and region from configuration.
     *
     * @param accessKey AWS Access Key
     * @param secretKey AWS Secret Key
     * @param region    AWS region identifier (e.g., "us-east-1", "ap-south-1")
     */
    public AwsConfig(
            @Value("${aws.access.key}") String accessKey,
            @Value("${aws.secret.key}") String secretKey,
            @Value("${aws.region}") String region) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.region = region;
    }

    /**
     * Creates a singleton {@link S3Client} bean for interacting with AWS S3.
     * <p>
     * Uses static credentials for simplicity; in production, prefer environment-based or role-based authentication.
     * </p>
     *
     * @return configured {@link S3Client} instance
     */

    @Bean
    public S3Client s3Client(){
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey,secretKey);
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}
