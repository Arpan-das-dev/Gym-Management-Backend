package com.gym.notificationservice.Services;

import com.gym.notificationservice.Dto.AuthNotificationRequests.EmailOtpRequestDto;
import com.gym.notificationservice.Dto.AuthNotificationRequests.PhoneOtpRequestDto;
import com.gym.notificationservice.Dto.AuthNotificationRequests.SendCredentialRequestDto;
import com.gym.notificationservice.Dto.AuthNotificationRequests.WelcomeRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class AuthNotificationService {

    private final MailService mailService;
    private final SmsService smsService;
    private final TemplateEngine templateEngine;

    public void sendWelcomeMail(WelcomeRequestDto requestDto){
        Context context = new Context();
        context.setVariable("name",requestDto.getName());
        context.setVariable("gymId",requestDto.getEmail());
        context.setVariable("gmail",requestDto.getEmail());
        context.setVariable("phone",requestDto.getPhone());

        String body = templateEngine.process("welcome-mail",context);
        mailService.sendMail(requestDto.getEmail(), "Welcome To FitStudio",body);
    }

    public void SendCredentials(SendCredentialRequestDto requestDto) {
        Context context = new Context();
        context.setVariable("name",requestDto.getName());
        context.setVariable("password",requestDto.getPassword());

        String body = templateEngine.process("send-credentials",context);
        mailService.sendMail(requestDto.getEmail(), "User Credentials",body);
    }


    public void sendVerifyEmail(EmailOtpRequestDto requestDto) {
        Context context = new Context();
        context.setVariable("name",requestDto.getName());
        context.setVariable("otp",requestDto.getOtp());

        String body = templateEngine.process("verify-email",context);
        mailService.sendMail(requestDto.getEmail(), "Email Verification",body);
    }


    public void sendVerifyPhone(PhoneOtpRequestDto requestDto) {
        String sms = "Hello,"+ requestDto.getName()+ " Your FitStudio verification code is " +requestDto.getOtp() +
                " It is valid for 15 minutes. Do not share this code with anyone.";
        smsService.sendSms(requestDto.getPhone(), sms);
    }

    public void sendResetPasswordEmail( EmailOtpRequestDto requestDto) {
        Context context = new Context();
        context.setVariable("name",requestDto.getName());
        context.setVariable("otp",requestDto.getOtp());

        String body = templateEngine.process("reset-password",context);
        mailService.sendMail(requestDto.getEmail(), "Email Verification For Password Reset",body);
    }
}
