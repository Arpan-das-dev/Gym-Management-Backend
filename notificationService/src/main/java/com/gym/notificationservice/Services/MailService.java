package com.gym.notificationservice.Services;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class MailService {
    private final String sender;
    private final SendGrid sendGrid;

    public MailService(@Value("${app.mail.sender}") String sender, SendGrid sendGrid) {
        this.sender = sender;
        this.sendGrid = sendGrid;
    }

    @Async
    public CompletableFuture<String> sendMail(String to, String subject, String body) {
        Email from = new Email(sender);
        Email sendTo = new Email(to);
        Content content = new Content("text/html", body);
        Request request = new Request();
        Mail mail = new Mail(from, subject, sendTo, content);

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sendGrid.api(request);
            System.out.println("SendGrid Status: " + response.getStatusCode());

            String result = "Sent mail to " + to + " with status: " + response.getStatusCode();
            return CompletableFuture.completedFuture(result);

        } catch (IOException e) {
            log.warn(e.getLocalizedMessage());
            String errorResult = "Unable to sent mail due to " + e.getLocalizedMessage();
            return CompletableFuture.completedFuture(errorResult);
        }
    }

}
