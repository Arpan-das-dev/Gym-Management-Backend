package com.gym.notificationservice.Services;

import com.gym.notificationservice.Dto.AuthNotificationRequests.*;
import com.gym.notificationservice.Dto.PaymentNotificationDtos.Responses.GenericResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthNotificationService {

//    private final MailService mailService;
    private final SmsService smsService;
    private final TemplateEngine templateEngine;
    private final MailjetService mailjetService;

    public void sendWelcomeMail(WelcomeRequestDto requestDto){
        Context context = new Context();
        context.setVariable("name",requestDto.getName());
        context.setVariable("gymId",requestDto.getEmail());
        context.setVariable("gmail",requestDto.getEmail());
        context.setVariable("phone",requestDto.getPhone());

        String body = templateEngine.process("welcome-mail",context);
//        mailService.sendMail(requestDto.getEmail(), "Welcome To FitStudio",body);
        mailjetService.sendMail(requestDto.getEmail(), "Welcome To FitStudio",body);
    }

    public void SendCredentials(SendCredentialRequestDto requestDto) {
        Context context = new Context();
        context.setVariable("name",requestDto.getName());
        context.setVariable("password",requestDto.getPassword());

        String body = templateEngine.process("send-credentials",context);
//        mailService.sendMail(requestDto.getEmail(), "User Credentials",body);
        mailjetService.sendMail(requestDto.getEmail(), "User Credentials",body);
    }


    public void sendVerifyEmail(EmailOtpRequestDto requestDto) {
        Context context = new Context();
        context.setVariable("name",requestDto.getName());
        context.setVariable("otp",requestDto.getOtp());

        String body = templateEngine.process("verify-email",context);
//        mailService.sendMail(requestDto.getEmail(), "Email Verification",body);
        mailjetService.sendMail(requestDto.getEmail(), "Email Verification",body);
    }


    public void sendVerifyPhone(PhoneOtpRequestDto requestDto) {
        smsService.sendOtp(requestDto.getPhone(),requestDto.getPhone());
    }

    public void sendResetPasswordEmail( EmailOtpRequestDto requestDto) {
        Context context = new Context();
        context.setVariable("name",requestDto.getName());
        context.setVariable("otp",requestDto.getOtp());

        String body = templateEngine.process("reset-password",context);
//        mailService.sendMail(requestDto.getEmail(), "Email Verification For Password Reset",body);
        mailjetService.sendMail(requestDto.getEmail(), "Email Verification For Password Reset",body);
    }

    public GenericResponseDto sendMessageOrReportResolvedStatus(MessageOrReportNotificationRequestDto requestDto) {
//       return mailService.sendMail(requestDto.getSendTo(), requestDto.getSubject(), requestDto.getMessage());
        log.info("sending to {}", requestDto.getSendTo());
        mailjetService.sendMail(requestDto.getSendTo(), requestDto.getSubject(), requestDto.getMessage());
        return new GenericResponseDto("Mail sent successfully to user for deleting account");
    }
}
