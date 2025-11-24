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
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Service layer responsible for reactive user authentication and user detail retrieval.
 *
 * <p>This service provides core login functionality including:
 * <ul>
 *   <li>Reactive authentication via email or user ID</li>
 *   <li>Password verification with secure encoding</li>
 *   <li>JWT token generation for authenticated sessions</li>
 *   <li>User detail retrieval for profile display</li>
 *   <li>User verification before sensitive operations such as deletion</li>
 * </ul>
 *
 * <p>Caching strategies are applied to optimize user detail retrieval performance.
 *
 * <p>Logging statements at different levels provide visibility into login attempts,
 * verification checks, and error conditions for effective monitoring and troubleshooting.
 *
 * @author Arpan Das
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    private final SignedUpsRepository signedUpsRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Authenticates a user reactively by identifier (email or user ID) and password.
     *
     * <p>Verifies password against stored hash and generates JWT token on success.
     * Logs each authentication attempt, success, and failure.
     *
     * @param requestDto login request containing user identifier and plaintext password
     * @return Mono emitting a LoginResponseDto containing JWT and user role on success
     * @throws UnauthorizedAccessException if password validation fails
     */
    public Mono<LoginResponseDto> login(LogiInRequestDto requestDto) {
        log.info("Login attempt for identifier: {}", requestDto.getIdentifier());

        return loadUserByIdentifier(requestDto.getIdentifier())
                .flatMap(user -> {
                    if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
                        log.warn("Invalid password attempt for identifier: {}", requestDto.getIdentifier());
                        return Mono.error(new UnauthorizedAccessException("Invalid password"));
                    }
                    String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getId());
                    log.info("User authenticated successfully for identifier: {}", requestDto.getIdentifier());
                    return Mono.just(new LoginResponseDto(token, user.getRole().name()));
                });
    }

    /**
     * Retrieves user details reactively by identifier.
     *
     * <p>Logs the retrieval of user information.
     *
     * @param identifier user email or ID
     * @return Mono emitting SignupDetailsInfoDto mapping user's stored data
     * @throws UserNotFoundException if user does not exist
     */
    public Mono<SignupDetailsInfoDto> getUserDetails(String identifier) {
        log.info("Retrieving user details for identifier: {}", identifier);

        return loadUserByIdentifier(identifier)
                .map(this::infoMapper);
    }

    /**
     * Loads a user reactively by email or user ID.
     *
     * <p>Applies caching keyed by user's ID, when present.
     * Logs lookup activity at debug level.
     *
     * @param identifier email or ID string
     * @return Mono emitting a SignedUps user entity
     * @throws UserNotFoundException if user is not found
     */
    @Cacheable(value = "userInfo", key = "#result.map(u -> u.id).block()")
    public Mono<SignedUps> loadUserByIdentifier(String identifier) {
        log.debug("Looking up user for identifier: {}", identifier);

        Mono<SignedUps> userMono = identifier.contains("@")
                ? signedUpsRepository.findByEmail(identifier)
                : signedUpsRepository.findById(identifier);

        return userMono.switchIfEmpty(Mono.error(
                new UserNotFoundException("User with identifier '" + identifier + "' not found")
        ));
    }

    /**
     * Verifies user credentials before allowing account deletion or other sensitive operations.
     *
     * <p>Checks password correctness and logs verification requests and outcomes.
     *
     * @param requestDto login request containing identifier and password for verification
     * @return Mono emitting Boolean true if verification successful
     * @throws UnauthorizedAccessException if password does not match
     */
    public Mono<Boolean> verifyBeforeDelete(LogiInRequestDto requestDto) {
        log.info("Verification before delete requested for identifier: {}", requestDto.getIdentifier());

        return loadUserByIdentifier(requestDto.getIdentifier())
                .flatMap(user -> {
                    if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
                        log.warn("Invalid password verification attempt before delete for identifier: {}", requestDto.getIdentifier());
                        return Mono.error(new UnauthorizedAccessException("Invalid password"));
                    }
                    log.info("User verified successfully before delete for identifier: {}", requestDto.getIdentifier());
                    return Mono.just(true);
                });
    }

    /**
     * Maps internal SignedUps user entity to SignupDetailsInfoDto for data transfer.
     *
     * @param user internal user entity
     * @return DTO containing user's public profile information and verification statuses
     */
    public SignupDetailsInfoDto infoMapper(SignedUps user) {
        return SignupDetailsInfoDto.builder()
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
                .build();
    }
}
