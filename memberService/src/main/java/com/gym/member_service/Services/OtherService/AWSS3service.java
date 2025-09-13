package com.gym.member_service.Services.OtherService;

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

@Service
public class AWSS3service {
    private final S3Client s3Client;
    @Value("${aws.s3.bucket}")
    private final String  bucketName;

    public AWSS3service(@Value("${aws.s3.bucket}") String bucketName, S3Client s3Client) {
        this.bucketName = bucketName;
        this.s3Client = s3Client;
    }

    public String uploadImage(String memberId, MultipartFile imageFile){
        String extension = detectExtension(Objects.requireNonNull(imageFile.getOriginalFilename()));
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        String key = "members/" + memberId + "/" + datePath + "/" +UUID.randomUUID() + extension;

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

    private String publicUrlBuilder(String key) {
        return "https://" + bucketName + ".s3.amazonaws.com/" + key;
    }

    private String detectExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.'));
    }

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
