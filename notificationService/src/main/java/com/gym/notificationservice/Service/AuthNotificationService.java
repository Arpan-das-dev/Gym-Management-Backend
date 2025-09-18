package com.gym.notificationservice.Service;


import com.gym.notificationservice.Dto.AuthNotificationRequests.EmailOtpRequestDto;
import com.gym.notificationservice.Dto.AuthNotificationRequests.PhoneOtpRequestDto;
import com.gym.notificationservice.Dto.AuthNotificationRequests.SendCredentialRequestDto;
import com.gym.notificationservice.Dto.AuthNotificationRequests.WelcomeRequestDto;
<<<<<<< Updated upstream
import lombok.RequiredArgsConstructor;
=======
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
>>>>>>> Stashed changes
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

<<<<<<< Updated upstream
/**
=======
import java.io.IOException;
/*
>>>>>>> Stashed changes
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
*/
@Service
@RequiredArgsConstructor
public class AuthNotificationService {
    /*
    AWS SES/SNS clients are commented out because we are not using them currently
    private final SesClient sesClient;
    private final SnsClient snsClient;
    */
    private final TemplateEngine templateEngine;
<<<<<<< Updated upstream
    private final MailService mailService;
    private final SmsService smsService;
=======
    private final SendGrid sendGrid;
    @Value("${app.mail.sender}")
    private String sender;
    @Value("${twilio.phone.number}")
    private String fromNumber;
>>>>>>> Stashed changes

    public void sendWelcomeMail(WelcomeRequestDto requestDto) {
        Context context = new Context();
        context.setVariable("name", requestDto.getName());
        context.setVariable("gymId", requestDto.getId());
        context.setVariable("gmail",requestDto.getEmail());
        context.setVariable("phone",requestDto.getPhone());
<<<<<<< Updated upstream
        String body = templateEngine.process("welcome-mail", context);
        mailService.sendMail(requestDto.getEmail(), "Welcome to FitStudio", body);
=======
        String body = templateEngine.process("Welcome Mail", context);
        sendMail(requestDto.getEmail(), "Welcome to FitStudio", body);
>>>>>>> Stashed changes
    }

    public void sendVerifyEmail(EmailOtpRequestDto requestDto) {
        Context context = new Context();
        context.setVariable("otp", requestDto.getOtp());
        context.setVariable("name", requestDto.getName());
        String body = templateEngine.process("verify-email", context);
<<<<<<< Updated upstream
        mailService.sendMail(requestDto.getEmail(), "Verify Your FitStudio Account ✅", body);
=======
        sendMail(requestDto.getEmail(), "Verify Your FitStudio Account ✅", body);
>>>>>>> Stashed changes
    }

    public void SendCredentials(SendCredentialRequestDto requestDto) {
        Context context = new Context();
        context.setVariable("name", requestDto.getName());
        context.setVariable("password", requestDto.getPassword());
        String body = templateEngine.process("send-credentials", context);
<<<<<<< Updated upstream
        mailService.sendMail(requestDto.getEmail(), "Your Fit Gym Account Credentials 🔑", body);
=======
        sendMail(requestDto.getEmail(), "Your Fit Gym Account Credentials 🔑", body);
>>>>>>> Stashed changes
    }

    public void sendResetPasswordEmail(EmailOtpRequestDto requestDto) {
        Context context = new Context();
        context.setVariable("name", requestDto.getEmail());
        context.setVariable("otp", requestDto.getOtp());
        String body = templateEngine.process("reset-password", context);
<<<<<<< Updated upstream
        mailService.sendMail(requestDto.getEmail(), "Reset Password", body);
=======
        sendMail(requestDto.getEmail(), "Reset Password", body);
    }

    public void sendMail(String to, String subject, String body) {
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
        } catch (IOException e) {
            throw new RuntimeException("error to process message");
        }
>>>>>>> Stashed changes
    }

    public void sendVerifyPhone(PhoneOtpRequestDto requestDto) {
        String message = "Hello " + requestDto.getName() +
                " this is your FitStudio verification OTP " + requestDto.getOtp() +
                "\n please verify your mobile no.";
<<<<<<< Updated upstream
        smsService.sendSms(requestDto.getPhone(), message);
    }

=======
        sendSms(requestDto.getPhone(), message);
    }

    public void sendSms(String to, String sms) {
        Message.creator(
                new PhoneNumber(to),
                new PhoneNumber(fromNumber),
                sms
        ).create();
    }


>>>>>>> Stashed changes
   /* public void sendVerifyPhone(PhoneOtpRequestDto requestDto) {
        String message = "Hello "+requestDto.getName()+
                " this is your FitStudio verification OTP " + requestDto.getOtp() +
                "\n please verify your mobile no.";
        snsClient.publish(PublishRequest.builder()
                .phoneNumber(requestDto.getPhone())
                .message(message)
                .build());
    }
    no need to create method for otp sending via phone because we are not using aws SES and SNS because we
    don't have any domain to get out of sandbox so we are using sendGrid

    private void sendEmail(String to, String subject, String body) {
        Destination destination = Destination.builder().toAddresses(to).build();
        Content content = Content.builder().data(subject).build();
        Content htmlContent = Content.builder().data(body).build();
        Body mailBody = Body.builder().html(htmlContent).build();
        Message message = Message.builder()
                .subject(content)
                .body(mailBody)
                .build();
        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .destination(destination)
                .message(message)
                .source(sender)
                .build();
        sesClient.sendEmail(emailRequest);
    }
     */
}
