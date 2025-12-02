package com.gym.authservice.Service;

import com.gym.authservice.Dto.Request.AdminCreationRequestDto;
import com.gym.authservice.Dto.Request.ApprovalRequestDto;
import com.gym.authservice.Dto.Request.SignupRequestDto;
import com.gym.authservice.Dto.Response.CredentialNotificationDto;
import com.gym.authservice.Dto.Response.SignUpResponseDto;
import com.gym.authservice.Dto.Response.SignupDetailsInfoDto;
import com.gym.authservice.Entity.SignedUps;
import com.gym.authservice.Exceptions.Custom.UserNotFoundException;
import com.gym.authservice.Repository.SignedUpsRepository;
import com.gym.authservice.Roles.RoleType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Service layer responsible for managing user accounts including members, trainers, and admins.
 *
 * <p>This service handles:
 * <ul>
 *   <li>Creation of different user types by an admin with notification dispatch</li>
 *   <li>User deletion by identifier with cache eviction</li>
 *   <li>Approval workflow for user accounts including role assignment and cache updates</li>
 *   <li>Cache management to maintain up-to-date user information</li>
 * </ul>
 *
 * <p>Implements transactional data consistency and integrates with WebClientService for asynchronous notifications.
 * Logging at various levels tracks key operations and failure scenarios for observability.

 * Usage of reactive Mono supports asynchronous operation and event-driven processing.
 *
 * @author Arpan Das
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final SignUpService signUpService;
    private final WebClientService webClientService;
    private final SignedUpsRepository signedUpsRepository;
    private final LoginService logiInService;
    private final CacheManager cacheManager;

    /**
     * Creates a new member via admin request.
     *
     * <p>Delegates signup to SignUpService, then sends notification of credentials.
     * Logs creation attempt and sends notification asynchronously.
     *
     * @param requestDto member signup request containing user details
     * @return Mono emitting sign-up response data including confirmation info
     */
    public Mono<SignUpResponseDto> createMemberByAdmin(SignupRequestDto requestDto){
        log.info("Admin requested member creation for email: {}", requestDto.getEmail());
        Mono<SignUpResponseDto> responseDto = signUpService.signUp(requestDto);
        CredentialNotificationDto notificationDto = CredentialNotificationDto.builder()
                .email(requestDto.getEmail())
                .name(requestDto.getFirstName() + " " + requestDto.getLastName())
                .password(requestDto.getPassword())
                .build();
        sendCredentialsNotification(notificationDto);
        return responseDto;
    }

    /**
     * Creates a new trainer via admin request.
     *
     * <p>Delegates signup, sends credentials notification.
     * Logs key events as in member creation.
     *
     * @param requestDto trainer signup request data
     * @return Mono emitting sign-up confirmation data
     */
    public Mono<SignUpResponseDto> createTrainerByAdmin(SignupRequestDto requestDto){
        log.info("Admin requested trainer creation for email: {}", requestDto.getEmail());
        Mono<SignUpResponseDto> responseDto = signUpService.signUp(requestDto);
        CredentialNotificationDto notificationDto = CredentialNotificationDto.builder()
                .email(requestDto.getEmail())
                .name(requestDto.getFirstName() + " " + requestDto.getLastName())
                .password(requestDto.getPassword())
                .build();
        sendCredentialsNotification(notificationDto);
        return responseDto;
    }

    /**
     * Creates a new admin user via an existing admin request.
     *
     * <p>Handles signup and sends credential notification securely.
     * Logs administrative user creation events.
     *
     * @param requestDto admin creation request containing user info
     * @return Mono emitting confirmation of admin creation
     */
    public Mono<SignUpResponseDto> createAdminByAdmin(@Valid AdminCreationRequestDto requestDto) {
        log.info("Admin requested creation of new admin for email: {}", requestDto.getEmail());
        Mono<SignUpResponseDto> responseDto = signUpService.createAdmin(requestDto);
        CredentialNotificationDto notificationDto = CredentialNotificationDto.builder()
                .email(requestDto.getEmail())
                .name(requestDto.getFirstName() + " " + requestDto.getLastName())
                .password(requestDto.getPassword())
                .build();
        sendCredentialsNotification(notificationDto);
        return responseDto;
    }

    /**
     * Deletes a user by email or ID.
     *
     * <p>Evicts cache if deleting by ID.
     * Throws UserNotFoundException if user does not exist.
     * Logs deletion intent and results.
     *
     * @param identifier email or user ID
     * @return confirmation message upon successful deletion
     * @throws UserNotFoundException if user not found by given identifier
     */
    public String deleteByIdentifier(String identifier){
        log.info("Request received to delete user by identifier: {}", identifier);
        String message = "User deleted successfully";
        if (identifier.contains("@")) {
            try {
                signedUpsRepository.deleteByEmail(identifier);
                log.info("User deleted by email: {}", identifier);
            } catch (UserNotFoundException e) {
                log.error("User not found for deletion by email: {}", identifier);
                throw new UserNotFoundException("User with this email does not exist");
            }
        } else {
            try {
                evictCache(identifier);
                signedUpsRepository.deleteById(identifier);
                log.info("User deleted by ID: {}", identifier);
            } catch (UserNotFoundException e) {
                log.error("User not found for deletion by ID: {}", identifier);
                throw new UserNotFoundException("User with this id does not exist");
            }
        }
        return message;
    }

    /**
     * Sends credential notification asynchronously via WebClientService.
     *
     * @param notificationDto contains email, name, and password for notification
     */
    public void sendCredentialsNotification(CredentialNotificationDto notificationDto){
        log.info("Sending credentials notification to email: {}", notificationDto.getEmail());
        webClientService.sendCredentials(notificationDto);
    }

    /**
     * Approves or disapproves a user account based on admin request.
     *
     * <p>Assigns roles, updates approval status and triggers appropriate downstream notifications.
     * Caches are updated or evicted accordingly.
     * Logs all critical approval workflow steps and outcomes.
     *
     * @param requestDto approval request with email and approval flag
     * @return Mono emitting confirmation message of approval status
     * @throws UserNotFoundException if user not found by email
     */
    @Transactional
    public Mono<String> approve(ApprovalRequestDto requestDto) {
        log.info("Approval request for email: {} with status: {}", requestDto.getEmail(), requestDto.isApproval());

        return signedUpsRepository.findByEmail(requestDto.getEmail())
                .switchIfEmpty(Mono.error(new UserNotFoundException("No user found with this email id: " + requestDto.getEmail())))
                .flatMap(user -> {
                    if (requestDto.isApproval()) {
                        user.setApproved(true);

                        if (user.getRole().name().startsWith("TRAINER")) {
                            user.setRole(RoleType.TRAINER);
                            return signedUpsRepository.save(user)
                                    .doOnSuccess(webClientService::sendTrainerServiceToCreateNewTrainer)
                                    .thenReturn("Successfully approved " + user.getFirstName() + " " + user.getLastName());
                        } else {
                            user.setRole(RoleType.MEMBER);
                            return signedUpsRepository.save(user)
                                    .doOnSuccess(webClientService::sendMemberServiceToCreateNewMember)
                                    .doOnSuccess(this::updateCache)
                                    .thenReturn("Successfully approved " + user.getFirstName() + " " + user.getLastName());
                        }
                    } else {
                        evictCache(user.getId());
                        return signedUpsRepository.deleteByEmail(user.getEmail())
                                .thenReturn("User with this email: " + user.getEmail() + " is unapproved");
                    }
                });
    }

    /**
     * Updates the user cache entry with the latest user details.
     *
     * <p>Logs cache update success and handles failures by evicting cache to maintain consistency.
     *
     * @param user the updated user entity
     */
    public void updateCache(SignedUps user) {
        log.info("Updating cache for user with id {}", user.getId());
        SignupDetailsInfoDto infoDto = logiInService.infoMapper(user);
        try {
            Objects.requireNonNull(cacheManager.getCache("userInfo")).put(user.getId(), infoDto);
            log.debug("Cache updated successfully for user id {}", user.getId());
        } catch (Exception e) {
            log.warn("Failed to update cache for user {}. Evicting cache. Error: {}", user.getId(), e.getMessage());
            evictCache(user.getId());
        }
    }

    /**
     * Evicts user cache entry by user ID.
     *
     * @param id the user ID whose cache is to be evicted
     */
    public void evictCache(String id) {
        log.info("Evicting cache for user id {}", id);
        Objects.requireNonNull(cacheManager.getCache("userInfo")).evict(id);
    }


}
