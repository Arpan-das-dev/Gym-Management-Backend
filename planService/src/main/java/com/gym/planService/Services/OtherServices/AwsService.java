package com.gym.planService.Services.OtherServices;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class AwsService {

    private final S3Client s3client;
    private final String bucketName;

    public AwsService(S3Client s3client,
                      @Value("${aws.s3.bucket}") String bucketName) {
        this.s3client = s3client;
        this.bucketName = bucketName;
    }

    @Async("uploadExecutor")
    public CompletableFuture<String> uploadPaymentReceipt(byte[] pdfBytes, String paymentId) {
        try {
            String fileName = "receipts/receipt_" + paymentId + ".pdf";
            log.info("Starting S3 upload for file: {}", fileName);

            PutObjectRequest request = PutObjectRequest.builder()
                    .contentType("application/pdf")
                    .key(fileName)
                    .bucket(bucketName)
                    .build();

            s3client.putObject(request, RequestBody.fromBytes(pdfBytes));

            String url = "https://" + bucketName + ".s3.amazonaws.com/" + fileName;
            log.info("S3 upload completed, URL: {}", url);
            return CompletableFuture.completedFuture(url);
        } catch (Exception ex) {
            log.error("S3 upload failed for payment ID {}: {}", paymentId, ex.getMessage());
            return CompletableFuture.failedFuture(ex);
        }
    }
}
