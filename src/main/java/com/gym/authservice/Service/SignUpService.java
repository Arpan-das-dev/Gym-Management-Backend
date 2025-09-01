package com.gym.authservice.Service;

import com.gym.authservice.Dto.Request.SignupRequestDto;
import com.gym.authservice.Dto.Response.*;
import com.gym.authservice.Entity.SignedUps;
import com.gym.authservice.Exceptions.Custom.DuplicateUserException;
import com.gym.authservice.Repository.SignedUpsRepository;
import com.gym.authservice.Roles.RoleType;
import com.gym.authservice.Utils.IdGenerationUtil;
import com.gym.authservice.Utils.OtpGenerationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import javax.swing.*;

@Service
@RequiredArgsConstructor
public class SignUpService {
    private final SignedUpsRepository signedUpsRepository;
    private final PasswordEncoder encoder;
    private final IdGenerationUtil idGenerationUtil;
    private final OtpGenerationUtil generationUtil;
    private final VerificationService verificationService;
    private final NotificationService notificationService;
    private final WebClient.Builder webClient;

    @Value("${admin.approval.url}")
    private final String approveUrl ;

    @Transactional
    public SignUpResponseDto signUp(SignupRequestDto requestDto) {
        boolean emailExists = signedUpsRepository.existsByEmail(requestDto.getEmail());
        boolean phoneExists = signedUpsRepository.existsByPhone(requestDto.getPhone());
        if (emailExists || phoneExists) {
            throw new DuplicateUserException("User with this email or phone number already exists");
        }

        SignedUps signedUps = SignedUps.builder()
                .id(idGenerationUtil.idGeneration(
                        requestDto.getRole().name(),
                        requestDto.getGender(),
                        requestDto.getJoinDate()
                ))
                .firstName(requestDto.getFirstName())
                .lastName(requestDto.getLastName())
                .email(requestDto.getEmail())
                .phone(requestDto.getPhone())
                .joinDate(requestDto.getJoinDate())
                .password(encoder.encode(requestDto.getPassword()))
                .gender(requestDto.getGender())
                .role(requestDto.getRole())
                .build();

        ApproveResponseDto responseDto = new ApproveResponseDto(requestDto.getEmail(), requestDto.getPhone(),
                requestDto.getFirstName() + " " + requestDto.getLastName(),
                requestDto.getRole(), requestDto.getJoinDate()
        );

        if (requestDto.getRole() == RoleType.TRAINER) {
            signedUps.setRole(RoleType.TRAINER_PENDING);
            sendApprovalRequest(approveUrl,responseDto);
        } else if (requestDto.getRole().equals(RoleType.TRAINER_ADMIN)) {
            signedUps.setRole(RoleType.TRAINER);
            signedUps.setApproved(true);
        } else {
            signedUps.setRole(RoleType.MEMBER);
            sendApprovalRequest(approveUrl,responseDto);
        }
        signedUpsRepository.save(signedUps);
        SignupNotificationDto notificationDto = new SignupNotificationDto(signedUps.getId(),
                signedUps.getEmail(),signedUps.getPhone(),signedUps.getFirstName()+" "+
                signedUps.getLastName());

        notificationService.sendWelcome(notificationDto);
        sendEmailOtp(signedUps.getEmail());
        sendPhoneOtp(signedUps.getPhone());

        return new SignUpResponseDto("user created successfully \n please verify your mobile no and email id");
    }

    public void sendApprovalRequest(String url,ApproveResponseDto responseDto){
        webClient.build().post()
                .uri(url)
                .bodyValue(responseDto)
                .retrieve().toBodilessEntity().subscribe();
    }

    public void sendEmailOtp(String email){
        String otpEmail = generationUtil.generateOtp(6);
        verificationService.StoreEmailOtp(email, otpEmail, 900);
        notificationService.sendEmailOtp(new EmailOtpNotificationDto(email, otpEmail));
    }
    public void sendPhoneOtp(String phone){
        String otpPhone = generationUtil.generateOtp(6);
        verificationService.StorePhoneOtp(phone, otpPhone, 900);
        notificationService.sendPhoneOtp(new PhoneOtpNotificationDto(phone, otpPhone));
    }
}
