package com.gym.authservice.Service;

import com.gym.authservice.Dto.Request.LogiInRequestDto;
import com.gym.authservice.Dto.Response.LoginResponseDto;
import com.gym.authservice.Dto.Response.SignupDetailsInfoDto;
import com.gym.authservice.Entity.SignedUps;
import com.gym.authservice.Exceptions.Custom.UnauthorizedAccessException;
import com.gym.authservice.Exceptions.Custom.UserNotFoundException;
import com.gym.authservice.Repository.SignedUpsRepository;
import com.gym.authservice.Config.Jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class LogiInService {

    private final SignedUpsRepository signedUpsRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Handles user login reactively.
     */
    public Mono<LoginResponseDto> login(LogiInRequestDto requestDto) {
        return loadUserByIdentifier(requestDto.getIdentifier())
                .flatMap(user -> {
                    if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
                        return Mono.error(new UnauthorizedAccessException("Invalid password"));
                    }
                    String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getId());
                    return Mono.just(new LoginResponseDto(token, user.getRole().name()));
                });
    }

    /**
     * Fetches user details reactively.
     */
    public Mono<SignupDetailsInfoDto> getUserDetails(String identifier) {
        return loadUserByIdentifier(identifier)
                .map(user -> SignupDetailsInfoDto.builder()
                        .id(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .gender(user.getGender())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .role(user.getRole())
                        .joinDate(user.getJoinDate())
                        .emailVerified(user.isEmailVerified())
                        .phoneVerified(user.isPhoneVerified())
                        .isApproved(user.isApproved())
                        .build());
    }

    /**
     * Loads user by ID or email reactively.
     */
    @Cacheable(value = "userInfo", key = "#identifier")
    public Mono<SignedUps> loadUserByIdentifier(String identifier) {
        Mono<SignedUps> userMono = identifier.contains("@")
                ? signedUpsRepository.findByEmail(identifier)
                : signedUpsRepository.findById(identifier);

        return userMono.switchIfEmpty(Mono.error(
                new UserNotFoundException("User with identifier '" + identifier + "' not found")
        ));
    }

    /**
     * Verifies user before deletion reactively.
     */
    public Mono<Boolean> verifyBeforeDelete(LogiInRequestDto requestDto) {
        return loadUserByIdentifier(requestDto.getIdentifier())
                .flatMap(user -> {
                    if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
                        return Mono.error(new UnauthorizedAccessException("Invalid password"));
                    }
                    return Mono.just(true);
                });
    }
}
