package com.gym.notificationservice.Services;

import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.resource.Emailv31;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class MailjetService {

    private final MailjetClient mailjetClient;
    private final String mailSender;
    private final String senderName;

    public MailjetService(MailjetClient mailjetClient,
                          @Value("${mailjet.mail.sender}") String mailSender,
                          @Value("${mailjet.name.sender}") String senderName) {
        this.mailjetClient = mailjetClient;
        this.mailSender = mailSender;
        this.senderName = senderName;
    }

    @Async
    public CompletableFuture<String> sendMail(String toEmail, String subject, String htmlContent) {

        if (!StringUtils.hasText(toEmail)) {
            String errorMessage = "no member exists for email :" + toEmail;
            log.error("Mail validation failed: Recipient email address is null or empty. Returning: {}", errorMessage);
            return CompletableFuture.completedFuture(errorMessage);
        }

        if (!StringUtils.hasText(htmlContent)) {
            String errorMessage = "Mail validation failed for " + toEmail + ": HTML content body is null or empty.";
            log.error(errorMessage);
            return CompletableFuture.completedFuture(errorMessage);
        }
        // ----------------------------------------------------

        if (this.mailjetClient == null) {
            String errorMessage = "Mailjet client not initialized. Cannot send email to " + toEmail;
            log.error(errorMessage);
            return CompletableFuture.completedFuture(errorMessage);
        }

        log.info("Attempting to send mail to {} asynchronously...", toEmail);

        // 1. Build the JSON request body required by Mailjet API v3.1
        MailjetRequest request = new MailjetRequest(Emailv31.resource)
                .property(Emailv31.MESSAGES, new JSONArray()
                        .put(new JSONObject()
                                .put(Emailv31.Message.FROM, new JSONObject()
                                        .put("Email", mailSender)
                                        .put("Name", senderName))
                                .put(Emailv31.Message.TO, new JSONArray()
                                        .put(new JSONObject()
                                                .put("Email", toEmail)))
                                .put(Emailv31.Message.SUBJECT, subject)
                                .put(Emailv31.Message.HTMLPART, htmlContent)));

        return processRequest(request,toEmail);
    }

    public void sendMailWithAttachment(String mailId, String subject, String body, MultipartFile attachment) {
        // convert the attachment into byte array []
        long start = System.currentTimeMillis();
        byte [] attachmentBytes = null;
        String fileName = null;
        String mimeType = null;
        // check if the attachment is null or empty
        if(attachment != null && !attachment.isEmpty()) {
            try {
                attachmentBytes = attachment.getBytes();
                fileName = attachment.getOriginalFilename();
                mimeType = attachment.getContentType();

                if (mimeType == null) {
                    mimeType = "application/octet-stream"; // if mime type is null going with defaults
                }
            }catch (IOException ex) {
                log.warn("failed to read file to send as attachment, calling default mail sending");
                sendMail(mailId,subject,body);
            }
        }
       sendAttachmentMail(mailId, subject, body, attachmentBytes, fileName, mimeType)

                .thenAcceptAsync(result -> {
                    // Logging success/final status on the completion thread
                    log.info("Mail sent successfully in background: {} and completed in {} ms",
                            result,System.currentTimeMillis()-start);
                })
                .exceptionally(ex -> {
                    // Logging any unhandled exception that occurred
                    log.error("Mail sending failed in background for {} due to: {}", mailId, ex.getMessage());
                    return null; // Return null to prevent propagation
                });
    }

    private CompletableFuture<String> sendAttachmentMail(String mailId, String subject, String body, byte[] attachmentBytes,
                                    String fileName, String mimeType)
    {
        if (this.mailjetClient == null) {
            String error = "Mailjet client not initialized. Cannot send email to " + mailId;
            log.error("unable to send mail due to {}",error);
        }

        log.info("attempting to send... mail to {}",mailId);
        JSONArray attachmentsArray = new JSONArray();
        if(attachmentBytes != null && attachmentBytes.length > 0) {
            String base64Content = java.util.Base64.getEncoder().encodeToString(attachmentBytes);

            // Build the Mailjet attachment object
            JSONObject attachmentObject = new JSONObject()
                    .put("ContentType", mimeType) // e.g., "application/pdf"
                    .put("Filename", fileName)     // e.g., "receipt.pdf"
                    .put("Base64Content", base64Content);

            attachmentsArray.put(attachmentObject);
        }

        JSONObject message = new JSONObject()
                .put(Emailv31.Message.FROM, new JSONObject()
                        .put("Email", mailSender)
                        .put("Name", senderName))
                .put(Emailv31.Message.TO, new JSONArray()
                        .put(new JSONObject()
                                .put("Email", mailId)))
                .put(Emailv31.Message.SUBJECT, subject)
                .put(Emailv31.Message.HTMLPART, body);

        // CRITICAL: Conditionally add the Attachments array if it's not empty
        if (!attachmentsArray.isEmpty()) {
            message.put(Emailv31.Message.ATTACHMENTS, attachmentsArray);
        }

        MailjetRequest request = new MailjetRequest(Emailv31.resource)
                .property(Emailv31.MESSAGES, new JSONArray().put(message));
       return processRequest(request,mailId);
    }

    private CompletableFuture<String> processRequest(MailjetRequest request, String toEmail) {
        try {
            // 2. Execute the request
            MailjetResponse response = mailjetClient.post(request);

            // 3. Check and log the result
            if (response.getStatus() >= 200 && response.getStatus() < 300) {
                String successMessage = "Mailjet email sent successfully to " + toEmail + ". Status: " + response.getStatus();
                log.info(successMessage);
                return CompletableFuture.completedFuture("Mail sent successfully to " + toEmail);
            } else {
                String failureMessage = "Mailjet failed to send email to " + toEmail + ". Status: " + response.getStatus() + ". Response: " + response.getData().toString();
                log.error(failureMessage);
                return CompletableFuture.completedFuture("Failed to send mail to " + toEmail);
            }
        } catch (Exception e) {
            String exceptionMessage = "Exception occurred while sending email via Mailjet to " + toEmail + ": " + e.getMessage();
            log.error(exceptionMessage, e);
            return CompletableFuture.completedFuture("Unable to send mail due to: " + e.getLocalizedMessage());
        }
    }
}

