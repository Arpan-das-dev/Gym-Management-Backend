package com.gym.planService.Services.OtherServices;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class AwsService {

    private final S3Client s3client;
    private final String bucketName;

    public AwsService(S3Client s3client,
                      @Value("${aws.s3.bucket}") String bucketName) {
        this.s3client = s3client;
        this.bucketName = bucketName;
    }

    public String uploadPaymentReceipt(byte[] pdfBytes, String paymentId) {
        String fileName = "receipts/receipt_" + paymentId + ".pdf";

        PutObjectRequest request = PutObjectRequest.builder()
                .contentType("application/pdf")
                .key(fileName)
                .bucket(bucketName)
                .build();
        s3client.putObject(request, RequestBody.fromBytes(pdfBytes));
        return "https://" + bucketName + ".s3.amazonaws.com/" + fileName;
    }
}
