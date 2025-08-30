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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

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
        if (requestDto.getRole() == RoleType.TRAINER) signedUps.setRole(RoleType.TRAINER_PENDING);
        signedUpsRepository.save(signedUps);

        webClient.build().post()
                .uri("http://localhost:8082/fitStudio/admin/approve")
                .bodyValue(
                        new ApproveResponseDto(
                                requestDto.getEmail(), requestDto.getPhone(),
                                requestDto.getFirstName() + " " + requestDto.getLastName(),
                                requestDto.getRole(), requestDto.getJoinDate()
                        ))
                .retrieve().toBodilessEntity().subscribe();

        notificationService.sendWelcome(
                new SignupNotificationDto(signedUps.getId(),
                        signedUps.getEmail(), signedUps.getPhone(),
                        signedUps.getFirstName() +" "+ signedUps.getLastName())
        );

        String otpEmail = generationUtil.generateOtp(6);
        String otpPhone = generationUtil.generateOtp(6);

        verificationService.StoreEmailOtp(signedUps.getEmail(), otpEmail, 900);
        verificationService.StorePhoneOtp(signedUps.getPhone(), otpPhone, 900);

        notificationService.sendEmailOtp(new EmailOtpNotificationDto(signedUps.getEmail(), otpEmail));
        notificationService.sendPhoneOtp(new PhoneOtpNotificationDto(signedUps.getPhone(), otpPhone));

        return new SignUpResponseDto("user created successfully");
    }

}
