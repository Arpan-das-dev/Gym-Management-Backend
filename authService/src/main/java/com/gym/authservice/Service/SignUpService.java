package com.gym.authservice.Service;

import com.gym.authservice.Dto.Request.AdminCreationRequestDto;
import com.gym.authservice.Dto.Request.SignupRequestDto;
import com.gym.authservice.Dto.Response.*;
import com.gym.authservice.Entity.SignedUps;
import com.gym.authservice.Exceptions.Custom.DuplicateUserException;
import com.gym.authservice.Repository.SignedUpsRepository;
import com.gym.authservice.Roles.RoleType;
import com.gym.authservice.Utils.IdGenerationUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service

public class SignUpService {
    private final SignedUpsRepository signedUpsRepository;
    private final PasswordEncoder encoder;
    private final IdGenerationUtil idGenerationUtil;
    private final WebClientService notificationService;
    private final WebClient.Builder webClient;
    private final String approveUrl ;

    public SignUpService(SignedUpsRepository signedUpsRepository,
                         PasswordEncoder encoder,
                         IdGenerationUtil idGenerationUtil,
                         WebClientService notificationService,
                         WebClient.Builder webClient,
                         @Value("${admin.approval.url}") String approveUrl) {
        this.signedUpsRepository = signedUpsRepository;
        this.encoder = encoder;
        this.idGenerationUtil = idGenerationUtil;
        this.notificationService = notificationService;
        this.webClient = webClient;
        this.approveUrl = approveUrl;
    }

    @Transactional
    public Mono<SignUpResponseDto> signUp(SignupRequestDto requestDto) {
        return validateUserUniqueness(requestDto.getEmail(), requestDto.getPhone())
                .then(Mono.defer(() -> {

                    // ✅ Step 1: Build the SignedUps entity
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
                            .approved(false)
                            .build();

                    // ✅ Step 2: Prepare approval DTO
                    ApproveResponseDto approvalDto = new ApproveResponseDto(
                            requestDto.getEmail(),
                            requestDto.getPhone(),
                            requestDto.getFirstName() + " " + requestDto.getLastName(),
                            requestDto.getRole(),
                            requestDto.getJoinDate()
                    );

                    // ✅ Step 3: Role-specific setup
                    switch (requestDto.getRole()) {
                        case TRAINER -> {
                            signedUps.setRole(RoleType.TRAINER_PENDING);
                            sendApprovalRequest(approveUrl, approvalDto);
                        }
                        case TRAINER_ADMIN -> {
                            signedUps.setRole(RoleType.TRAINER);
                            signedUps.setApproved(true);
                        }
                        case ADMIN_ADMIN -> {
                            signedUps.setRole(RoleType.ADMIN);
                            signedUps.setApproved(true);
                        }
                        case MEMBER_ADMIN -> {
                            signedUps.setRole(RoleType.MEMBER);
                            signedUps.setApproved(true);
                        }
                        default -> {
                            signedUps.setRole(RoleType.MEMBER);
                            sendApprovalRequest(approveUrl, approvalDto);
                        }
                    }

                    // ✅ Step 4: Save user
                    return signedUpsRepository.insertSignedUp(
                                    signedUps.getId(),
                                    signedUps.getFirstName(),
                                    signedUps.getLastName(),
                                    signedUps.getGender(),
                                    signedUps.getEmail(),
                                    signedUps.getPhone(),
                                    signedUps.getPassword(),
                                    signedUps.getRole(),
                                    signedUps.getJoinDate(),
                                    signedUps.isEmailVerified(),
                                    signedUps.isPhoneVerified(),
                                    signedUps.isApproved()
                            )

                            // ✅ Step 5: Continue after successful insert
                            .then(Mono.defer(() -> {
                                SignupNotificationDto notificationDto = new SignupNotificationDto(
                                        signedUps.getId(),
                                        signedUps.getEmail(),
                                        signedUps.getPhone(),
                                        signedUps.getFirstName() + " " + signedUps.getLastName()
                                );

                                notificationService.sendWelcome(notificationDto);

                                return Mono.just(new SignUpResponseDto(
                                        "User created successfully.\nPlease verify your mobile number and email ID."
                                ));
                            }));
                }));
    }

    private Mono<Void> validateUserUniqueness(String email, String phone) {
        return Mono.zip(
                signedUpsRepository.existsByEmail(email),
                signedUpsRepository.existsByPhone(phone)
        ).flatMap(tuple -> {
            boolean emailExists = tuple.getT1();
            boolean phoneExists = tuple.getT2();

            if (emailExists || phoneExists) {
                return Mono.error(new DuplicateUserException("User with this email or phone number already exists"));
            }
            return Mono.empty();
        });
    }


    public void sendApprovalRequest(String url,ApproveResponseDto responseDto){
        webClient.build().post()
                .uri(url)
                .bodyValue(responseDto)
                .retrieve().toBodilessEntity().subscribe();
    }

    public Mono<SignUpResponseDto> createAdmin(AdminCreationRequestDto requestDto) {
        return validateUserUniqueness(requestDto.getEmail(), requestDto.getPhone())
                .then(Mono.defer(() -> {

                    // ✅ Step 1: Build the SignedUps entity
                    SignedUps signedUps = SignedUps.builder()
                            .id(requestDto.getId())
                            .firstName(requestDto.getFirstName())
                            .lastName(requestDto.getLastName())
                            .email(requestDto.getEmail())
                            .phone(requestDto.getPhone())
                            .joinDate(requestDto.getJoinDate())
                            .password(encoder.encode(requestDto.getPassword()))
                            .gender(requestDto.getGender())
                            .role(requestDto.getRole())
                            .approved(false)
                            .build();
                    signedUps.setRole(RoleType.ADMIN);
                    return signedUpsRepository.insertSignedUp(
                                    signedUps.getId(),
                                    signedUps.getFirstName(),
                                    signedUps.getLastName(),
                                    signedUps.getGender(),
                                    signedUps.getEmail(),
                                    signedUps.getPhone(),
                                    signedUps.getPassword(),
                                    signedUps.getRole(),
                                    signedUps.getJoinDate(),
                                    signedUps.isEmailVerified(),
                                    signedUps.isPhoneVerified(),
                                    signedUps.isApproved()
                            )

                            // ✅ Step 5: Continue after successful insert
                            .then(Mono.defer(() -> {
                                SignupNotificationDto notificationDto = new SignupNotificationDto(
                                        signedUps.getId(),
                                        signedUps.getEmail(),
                                        signedUps.getPhone(),
                                        signedUps.getFirstName() + " " + signedUps.getLastName()
                                );

                                notificationService.sendWelcome(notificationDto);

                                return Mono.just(new SignUpResponseDto(
                                        "User created successfully.\nPlease verify your mobile number and email ID."
                                ));
                            }));
                }));
    }
}
