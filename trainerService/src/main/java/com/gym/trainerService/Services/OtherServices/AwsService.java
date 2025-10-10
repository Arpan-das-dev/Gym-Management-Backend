package com.gym.trainerService.Services.OtherServices;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;
/**
 * Service layer responsible for managing AWS S3 file operations.
 * <p>
 * This class provides methods to upload and delete files (primarily trainer profile images)
 * from an AWS S3 bucket. It handles S3 key generation, file metadata setup, and
 * S3 client interaction in a clean and reusable way.
 * </p>
 *
 * <p><b>Key Features:</b></p>
 * <ul>
 *   <li>Uploads files to a structured path inside S3 (trainers/{trainerId}/{date}/...)</li>
 *   <li>Generates public S3 URLs for easy access</li>
 *   <li>Safely deletes files based on stored URLs</li>
 *   <li>Uses {@link S3Client} injected as a Spring Bean from {@code AwsConfig}</li>
 * </ul>
 *
 * <p><b>Best Practices:</b></p>
 * <ul>
 *   <li>Ensure S3 bucket policies allow required operations (PUT, DELETE, GET).</li>
 *   <li>Validate file size and type before uploading (especially for public URLs).</li>
 *   <li>In production, consider using pre-signed URLs for security-sensitive uploads.</li>
 * </ul>
 *
 * <p><b>Example Usage:</b></p>
 * <pre>
 * {@code
 * @Service
 * public class TrainerProfileService {
 *     private final AwsService awsService;
 *
 *     public TrainerProfileService(AwsService awsService) {
 *         this.awsService = awsService;
 *     }
 *
 *     public String uploadTrainerImage(String trainerId, MultipartFile image) {
 *         return awsService.uploadImage(trainerId, image);
 *     }
 * }
 * }
 * </pre>
 *
 * @author Arpan
 * @since 1.0
 */
@Service
public class AwsService {
    /** AWS S3 client injected from {@link com.gym.trainerService.Configs.AwsConfig}. */
    private final S3Client s3Client;

    /** Name of the S3 bucket where images are stored. */
    private final String bucketName;

    /**
     * Constructs the {@link AwsService} and injects dependencies.
     *
     * @param bucketName the name of the AWS S3 bucket
     * @param s3Client   the {@link S3Client} instance
     */
    public AwsService(@Value("${aws.s3.bucket}") String bucketName, S3Client s3Client) {
        this.bucketName = bucketName;
        this.s3Client = s3Client;
    }

    /**
     * Uploads a trainer's profile image to AWS S3 with a unique key structure.
     * <p>
     * The uploaded file is organized under:
     * {@code trainers/{trainerId}/{yyyy/MM/dd}/{UUID}.{extension}}
     * </p>
     *
     * @param trainerId  unique identifier of the trainer
     * @param imageFile  the image file to upload
     * @return public URL to access the uploaded image
     * @throws RuntimeException if file read or upload fails
     */
    public String uploadImage(String trainerId, MultipartFile imageFile){
        String extension = detectExtension(Objects.requireNonNull(imageFile.getOriginalFilename()));
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        String key = "trainers/" + trainerId + "/" + datePath + "/" + UUID.randomUUID() + extension;

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(imageFile.getContentType())
                .build();
        try {
            s3Client.putObject(objectRequest, RequestBody.fromBytes(imageFile.getBytes()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read files", e);
        }
        return publicUrlBuilder(key);
    }
    /**
     * Constructs the public S3 URL for an uploaded file.
     *
     * @param key the S3 object key
     * @return the public URL for accessing the object
     */
    private String publicUrlBuilder(String key) {
        return "https://" + bucketName + ".s3.amazonaws.com/" + key;
    }
    /**
     * Detects the file extension from the original filename.
     *
     * @param originalFilename the name of the uploaded file
     * @return file extension (e.g., ".png", ".jpg"), or empty string if not found
     */
    private String detectExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.'));
    }
    /**
     * Deletes an image from AWS S3 using its public URL.
     *
     * @param profileImageUrl the public S3 URL of the image to delete
     * @throws IllegalArgumentException if the URL does not match expected S3 format
     */
    public void deleteImage(String profileImageUrl) {

        String prefix = bucketName + ".s3.amazonaws.com/";
        int index = profileImageUrl.indexOf(prefix);
        if (index == -1) {
            throw new IllegalArgumentException("URL does not match expected S3 format");
        }

        String key = profileImageUrl.substring(index + prefix.length());

        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.deleteObject(deleteRequest);
    }
}
