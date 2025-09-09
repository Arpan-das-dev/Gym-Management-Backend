package com.gym.adminservice.Services.AwsServices;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

@Service
/*
 * This class is used to interact with the AWS S3 service
 * It uses the S3 client bean created in the S3Config class to perform
 * operations
 * It uses the bucket name from the application.properties file
 * It provides a method to upload an image to the S3 bucket and returns the
 * public URL of the uploaded image
 * and also to download the image from the S3 bucket and delete the image from
 * the S3 bucket
 */
public class AWSS3Service {

    // Injecting value from application.properties file for S3 bucket name
    @Value("${aws.s3.bucket}")
    private final String bucketName;
    private final S3Client s3Client;

    // Constructor to initialize the bucket name and S3 client
    public AWSS3Service(@Value("${aws.s3.bucket}") String bucketName, S3Client s3Client) {
        this.bucketName = bucketName;
        this.s3Client = s3Client;
    }

    /*
     * This method uploads an image to the S3 bucket
     * It takes the product ID and the image file as parameters
     * It generates a unique key for the image using the product ID and the current
     * date and time
     * and returns the public URL of the uploaded image to store the url in the
     * database
     */
    public String uploadImage(String productId, MultipartFile file) {
        String extension = detectExtension(Objects.requireNonNull(file.getOriginalFilename())); // Detecting the file
                                                                                                // extension
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")); // Generating a date
                                                                                                 // path using the
                                                                                                 // current
                                                                                                 // date in the format
                                                                                                 // yyyy/MM/dd

        String key = "products/" + productId + "/" + datePath + UUID.randomUUID() + extension; // Generating a unique
                                                                                               // key for the image
                                                                                               // using the product ID,
                                                                                               // date path, a random
                                                                                               // UUID and the file
                                                                                               // extension
                                                                                               // in the format
                                                                                               // products/{productId}/{datePath}/{UUID}.{extension}

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build(); // Creating a PutObjectRequest to upload the image to the S3 bucket and setting
                          // the bucket name, key and content type
        try {
            s3Client.putObject(objectRequest, RequestBody.fromBytes(file.getBytes())); // Uploading the image to the S3
                                                                                       // bucket using the S3 client and
                                                                                       // the PutObjectRequest
        } catch (IOException e) {
            throw new RuntimeException("Failed to read files", e); // Throwing a runtime exception if there is an error
                                                                   // reading the file
        }
        return buildPublicUrl(key); // Returning the public URL of the uploaded image to store the url in the
                                    // database using a helper method
    }

    /*
     * This helper method builds the public URL of the uploaded image
     * It takes the key of the image as a parameter
     * It returns the public URL of the image which is used in the uploadImage
     * method
     */
    private String buildPublicUrl(String key) {
        return "http://" + bucketName + ".s3.amazonaws.com/" + key;
    }

    /*
     * This helper method detects the file extension of the uploaded image
     * It takes the file name as a parameter
     * It returns the file extension of the image
     */
    private String detectExtension(String fileName) {
        if (fileName == null)
            return ""; // If the file name is null, return an empty string
        int index = fileName.lastIndexOf('.'); // Finding the last index of the dot in the file name
        if (index == -1)
            return ""; // If there is no dot in the file name, return an empty string
        return fileName.substring(index, fileName.length() - 1); // Returning the file extension from the last index of
                                                                 // the dot to the end of the file name
    }


    /*
     * as of now we don't need delete and download image methods
     * but in future if we need them we can uncomment them
     * when we actually build the product service and control them from admin service(microservice architecture)
     * so keeping them commented for now
     * because we have to pass the same information in the db to update all those things
     */
}
