package com.gym.authservice.Service;

import com.gym.authservice.Dto.Request.AdminCreationRequestDto;
import com.gym.authservice.Dto.Request.SignupRequestDto;
import com.gym.authservice.Dto.Response.*;
import com.gym.authservice.Entity.SignedUps;
import com.gym.authservice.Exceptions.Custom.DuplicateUserException;
import com.gym.authservice.Exceptions.Model.ErrorResponse;
import com.gym.authservice.Repository.SignedUpsRepository;
import com.gym.authservice.Roles.RoleType;
import com.gym.authservice.Utils.IdGenerationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
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
        log.info("Starting signup process for email: {} and role is {}" ,
                requestDto.getEmail(),requestDto.getRole());

        return validateUserUniqueness(requestDto.getEmail(), requestDto.getPhone())
                .doOnSuccess(v -> log.info("User uniqueness validated for email: {}", requestDto.getEmail()))
                .then(Mono.defer(() -> {

                    // Step 1: Build entity
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

                    ApproveResponseDto approvalDto = new ApproveResponseDto(
                            requestDto.getEmail(),
                            requestDto.getPhone(),
                            requestDto.getFirstName() + " " + requestDto.getLastName(),
                            requestDto.getRole(),
                            requestDto.getJoinDate()
                    );

                    Mono<Void> approvalFlow;

                    // ======= Role Logic =======
                    switch (requestDto.getRole()) {

                        case TRAINER -> {
                            signedUps.setRole(RoleType.TRAINER_PENDING);
                            log.info("Sending approval request for TRAINER: {} till then role is {}",
                                    signedUps.getEmail(),signedUps.getRole().name());
                            approvalFlow = sendApprovalRequestReactive(approveUrl, approvalDto)
                                    .doOnNext(resp -> log.info("Approval response for TRAINER {}: {}", signedUps.getEmail(), resp))
                                    .then();
                        }

                        case TRAINER_ADMIN -> {
                            signedUps.setRole(RoleType.TRAINER);
                            signedUps.setApproved(true);
                            log.info("Auto-approved TRAINER_ADMIN: {} and keeping the role as {}",
                                    signedUps.getEmail(),signedUps.getRole().name());
                            approvalFlow = Mono.empty();
                        }

                        case ADMIN_ADMIN -> {
                            signedUps.setRole(RoleType.ADMIN);
                            signedUps.setApproved(true);
                            log.info("Auto-approved ADMIN_ADMIN: {}", signedUps.getEmail());
                            approvalFlow = Mono.empty();
                        }

                        case MEMBER_ADMIN -> {
                            signedUps.setRole(RoleType.MEMBER);
                            signedUps.setApproved(true);
                            log.info("Auto-approved MEMBER_ADMIN: {}", signedUps.getEmail());
                            approvalFlow = Mono.empty();
                        }

                        default -> {
                            signedUps.setRole(RoleType.MEMBER);
                            log.info("Sending approval request for MEMBER: {}", signedUps.getEmail());
                            approvalFlow = sendApprovalRequestReactive(approveUrl, approvalDto)
                                    .doOnNext(resp -> log.info("Approval response for MEMBER {}: {}", signedUps.getEmail(), resp))
                                    .then();
                        }
                    }

                    // ========== FINAL FLOW ==========
                    return approvalFlow
                            .then(signedUpsRepository.insertSignedUp(
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
                            ))
                            .doOnSuccess(v -> log.info("User saved successfully: {}", signedUps.getEmail()))
                            .then(Mono.defer(() -> {
                                try {
                                    notificationService.sendWelcome(
                                            new SignupNotificationDto(
                                                    signedUps.getId(),
                                                    signedUps.getEmail(),
                                                    signedUps.getPhone(),
                                                    signedUps.getFirstName() + " " + signedUps.getLastName()
                                            )
                                    );
                                    log.info("Notification sent for user: {}", signedUps.getEmail());
                                } catch (Exception e) {
                                    log.error("Notification failed for user: {}", signedUps.getEmail(), e);
                                    // Notification failures do not affect signup
                                }

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


    public Mono<String> sendApprovalRequestReactive(String url, ApproveResponseDto dto) {
        log.info("Sending approval request to URL: {} for email: {}", url, dto.getEmail());
        return webClient.build()
                .post()
                .uri(url)
                .body(BodyInserters.fromValue(dto))
                .retrieve()
                .onStatus(
                        status -> !status.is2xxSuccessful(),
                        clientResponse -> clientResponse.bodyToMono(ErrorResponse.class)
                                .flatMap(err -> {
                                    log.error("Approval request failed for email: {} - {}", dto.getEmail(), err.getMessage());
                                    return Mono.error(new DuplicateUserException(err.getMessage()));
                                })
                )
                .bodyToMono(String.class)
                .doOnNext(resp -> log.info("Approval request succeeded for email: {} - response: {}", dto.getEmail(), resp));
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
