package com.gym.notificationservice.Service;


import com.gym.notificationservice.Dto.AuthNotificationRequests.EmailOtpRequestDto;
import com.gym.notificationservice.Dto.AuthNotificationRequests.PhoneOtpRequestDto;
import com.gym.notificationservice.Dto.AuthNotificationRequests.SendCredentialRequestDto;
import com.gym.notificationservice.Dto.AuthNotificationRequests.WelcomeRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
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
    private final MailService mailService;
    private final SmsService smsService;

    public void sendWelcomeMail(WelcomeRequestDto requestDto) {
        Context context = new Context();
        context.setVariable("name", requestDto.getName());
        context.setVariable("gymId", requestDto.getId());
        context.setVariable("gmail",requestDto.getEmail());
        context.setVariable("phone",requestDto.getPhone());
        String body = templateEngine.process("welcome-mail", context);
        mailService.sendMail(requestDto.getEmail(), "Welcome to FitStudio", body);
    }

    public void sendVerifyEmail(EmailOtpRequestDto requestDto) {
        Context context = new Context();
        context.setVariable("otp", requestDto.getOtp());
        context.setVariable("name", requestDto.getName());
        String body = templateEngine.process("verify-email", context);
        mailService.sendMail(requestDto.getEmail(), "Verify Your FitStudio Account ✅", body);
    }

    public void SendCredentials(SendCredentialRequestDto requestDto) {
        Context context = new Context();
        context.setVariable("name", requestDto.getName());
        context.setVariable("password", requestDto.getPassword());
        String body = templateEngine.process("send-credentials", context);
        mailService.sendMail(requestDto.getEmail(), "Your Fit Gym Account Credentials 🔑", body);
    }

    public void sendResetPasswordEmail(EmailOtpRequestDto requestDto) {
        Context context = new Context();
        context.setVariable("name", requestDto.getEmail());
        context.setVariable("otp", requestDto.getOtp());
        String body = templateEngine.process("reset-password", context);
        mailService.sendMail(requestDto.getEmail(), "Reset Password", body);
    }

    public void sendVerifyPhone(PhoneOtpRequestDto requestDto) {
        String message = "Hello " + requestDto.getName() +
                " this is your FitStudio verification OTP " + requestDto.getOtp() +
                "\n please verify your mobile no.";
        smsService.sendSms(requestDto.getPhone(), message);
    }

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
