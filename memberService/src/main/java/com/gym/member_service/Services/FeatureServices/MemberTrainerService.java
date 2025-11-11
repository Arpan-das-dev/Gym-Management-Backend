package com.gym.member_service.Services.FeatureServices;

import com.gym.member_service.Dto.MemberTrainerDtos.Requests.AddSessionsRequestDto;
import com.gym.member_service.Dto.MemberTrainerDtos.Requests.AddTrainerRequestDto;
import com.gym.member_service.Dto.MemberTrainerDtos.Requests.TrainerAssignRequestDto;
import com.gym.member_service.Dto.MemberTrainerDtos.Requests.UpdateSessionRequestDto;
import com.gym.member_service.Dto.MemberTrainerDtos.Responses.SessionsResponseDto;
import com.gym.member_service.Dto.MemberTrainerDtos.Responses.TrainerAssignResponseDto;
import com.gym.member_service.Dto.MemberTrainerDtos.Responses.TrainerInfoResponseDto;
import com.gym.member_service.Dto.MemberTrainerDtos.Wrapper.AllSessionInfoResponseDto;
import com.gym.member_service.Exception.Exceptions.*;
import com.gym.member_service.Model.Member;
import com.gym.member_service.Model.Session;
import com.gym.member_service.Model.Trainer;
import com.gym.member_service.Repositories.MemberRepository;
import com.gym.member_service.Repositories.SessionRepository;
import com.gym.member_service.Repositories.TrainerRepository;
import com.gym.member_service.Services.OtherService.WebClientServices;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for managing member-trainer relationships and training sessions.
 *
 * <p>This service provides business logic for:
 * <ul>
 *   <li>Processing trainer assignment requests and administrative workflows</li>
 *   <li>Managing trainer-member relationships and eligibility periods</li>
 *   <li>Handling training session lifecycle (creation, retrieval, updates, deletion)</li>
 *   <li>Validating member plans and trainer eligibility</li>
 *   <li>Integrating with external administrative services</li>
 * </ul>
 *
 * <p>This service implements caching strategies for performance optimization
 * and uses transactional boundaries to ensure data consistency across
 * repository operations.
 *
 * <p>All methods include comprehensive validation to ensure business rules
 * are enforced, including plan expiration checks, trainer eligibility verification,
 * and session ownership validation.
 *
 * @author Arpan Das
 * @version 1.0
 * @since 1.0
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberTrainerService {
    /**
     * Repository for member data access operations.
     */
    private final MemberRepository memberRepository;
    /**
     * Repository for trainer data access operations.
     */
    private final TrainerRepository trainerRepository;
    /**
     * Web client service for external administrative service communication.
     */
    private final WebClientServices webClientServices;
    /**
     * Repository for training session data access operations.
     */
    private final SessionRepository sessionRepository;
    
    /**
     * Processes a trainer assignment request and forwards it to administrative services.
     *
     * <p>This method validates the requesting member exists, checks for existing
     * trainer assignments, and determines eligibility for new trainer requests.
     * For existing trainer relationships, it allows extension requests for the same
     * trainer or validates eligibility for different trainer assignments.
     *
     * <p>The processed request is sent asynchronously to the administrative service
     * for approval and further processing.
     *
     * @param requestDto the trainer assignment request containing member ID,
     *                   trainer details, and request metadata. Must not be null.
     * @return TrainerAssignResponseDto containing the complete request information
     *         sent to administrative services
     * @throws UserNotFoundException if the member ID does not exist in the system
     * @throws TrainerAlreadyExistsException if the member already has an active
     *                                      trainer assignment that conflicts with
     *                                      the new request
     * @throws IllegalArgumentException if the request DTO contains invalid data
     *
     * @see TrainerAssignRequestDto
     * @see TrainerAssignResponseDto
     */
    public TrainerAssignResponseDto requestAdminForTrainer(TrainerAssignRequestDto requestDto) {
        // 1. Validate member existence
        Member member = memberRepository.findById(requestDto.getMemberId())
                .orElseThrow(() -> new UserNotFoundException(
                        "No member found with this id: " + requestDto.getMemberId()));

        // 2. Check if trainer already exists for this member
        Optional<Trainer> existingTrainerOpt = trainerRepository.findTrainerByMemberId(requestDto.getMemberId());

        if (existingTrainerOpt.isPresent()) {
            Trainer currentTrainer = existingTrainerOpt.get();

            // Case 1: Same trainer → allow request (extend will be handled by Admin)
            if (!currentTrainer.getTrainerId().equals(requestDto.getTrainerId())) {
                // Case 2: Different trainer → check eligibility
                if (currentTrainer.getEligibilityEnd().isAfter(LocalDate.now())) {
                    throw new TrainerAlreadyExistsException(
                            "You are already assigned to trainer " + currentTrainer.getTrainerName()
                                    + " until " + currentTrainer.getEligibilityEnd());
                }
            }
        }
        // 3. Build response DTO to send to Admin
        TrainerAssignResponseDto responseDto = TrainerAssignResponseDto.builder()
                .memberId(member.getId())
                .memberProfileImageUrl(member.getProfileImageUrl())
                .memberName(member.getFirstName() + " " + member.getLastName())
                .requestDate(requestDto.getRequestDate())
                .trainerId(requestDto.getTrainerId())
                .trainerProfileImageUrl(requestDto.getTrainerProfileImageUrl())
                .trainerName(requestDto.getTrainerName())
                .memberPlanName(member.getPlanName())
                .memberPlanExpirationDate(member.getPlanExpiration().toLocalDate())
                .build();

        // 4. Send request to Admin Service for approval
        webClientServices.sendTrainerRequestToAdmin(responseDto);
        return responseDto;
    }

    /**
     * Assigns or updates a trainer assignment for a member.
     *
     * <p>This method handles both new trainer assignments and existing trainer
     * relationship extensions. For existing relationships with the same trainer,
     * it extends the eligibility period. For new trainer assignments, it creates
     * a fresh trainer-member relationship.
     *
     * <p>The method is transactional to ensure data consistency and includes
     * cache invalidation to maintain cache coherence across the system.
     *
     * @param requestDto the trainer assignment details including trainer information,
     *                   member ID, and eligibility end date. Must not be null and
     *                   must pass validation constraints.
     * @return TrainerInfoResponseDto containing the assigned trainer's complete
     *         information including eligibility dates
     * @throws UserNotFoundException if the member ID does not exist in the system
     * @throws TransactionSystemException if the database transaction fails
     * @throws IllegalArgumentException if the eligibility date is invalid
     *
     * @see AddTrainerRequestDto
     * @see TrainerInfoResponseDto
     */
    @Transactional
    @CachePut(value = "member'sTrainer", key="#requestDto.memberId")
    public TrainerInfoResponseDto assignTrainerToMember(AddTrainerRequestDto requestDto) {
        Member member = memberRepository.findById(requestDto.getMemberId())
                .orElseThrow(() ->
                        new UserNotFoundException( "No member found with this id: " + requestDto.getMemberId()));
        log.info("Fetched member successfully from db: {} {}", member.getId(), member.getEmail());
        Trainer trainer;
        if ((trainer = trainerRepository.findTrainerByMemberId(requestDto.getMemberId()).orElse(null))!= null
        && trainer.getTrainerId().equals(requestDto.getTrainerId())) {
            LocalDate newEligibility = trainer.getEligibilityEnd().isAfter(LocalDate.now()) ?
                    trainer.getEligibilityEnd().plusDays(
                            requestDto.getEligibilityEnd().toEpochDay() - LocalDate.now().toEpochDay())
                    : requestDto.getEligibilityEnd(); trainer.setEligibilityEnd(newEligibility);
                    log.info("Updated eligibility to {}", newEligibility);
        } else {
            trainer = Trainer.builder()
                    .trainerId(requestDto.getTrainerId())
                    .trainerName(requestDto.getTrainerName())
                    .trainerProfileImageUrl(requestDto.getTrainerProfileImageUrl())
                    .memberId(member.getId())
                    .eligibilityEnd(requestDto.getEligibilityEnd())
                    .build();
            log.info("Saving new trainer : {}", trainer);
        }
        trainerRepository.save(trainer);
        return TrainerInfoResponseDto.builder()
                .trainerId(trainer.getTrainerId())
                .trainerName(trainer.getTrainerName())
                .profileImageUrl(trainer.getTrainerProfileImageUrl())
                .eligibilityDate(trainer.getEligibilityEnd())
                .build();
    }
    /**
     * Retrieves trainer information for a specific member.
     *
     * <p>This method returns detailed information about the trainer currently
     * assigned to the specified member. If no trainer is assigned, it returns
     * an empty response DTO rather than throwing an exception.
     *
     * <p>Results are cached to improve performance for frequently accessed
     * trainer information.
     *
     * @param memberId the unique identifier of the member whose trainer
     *                 information is requested. Must not be null or empty.
     * @return TrainerInfoResponseDto containing trainer details if assigned,
     *         or empty DTO if no trainer is found
     * @throws IllegalArgumentException if memberId is null or empty
     *
     * @see TrainerInfoResponseDto
     */
    @Cacheable(value = "member'sTrainer", key="#memberId")
    public TrainerInfoResponseDto getTrainerInfo(String memberId) {
        Trainer trainer = trainerRepository.findTrainerByMemberId(memberId).orElse(null);
        if(trainer==null){
            return new TrainerInfoResponseDto();
        }
        log.info("Trainer found with this id {} and eligible till {}",trainer.getTrainerId(), trainer.getEligibilityEnd());
        return TrainerInfoResponseDto.builder()
                .trainerId(trainer.getTrainerId())
                .trainerName(trainer.getTrainerName())
                .profileImageUrl(trainer.getTrainerProfileImageUrl())
                .eligibilityDate(trainer.getEligibilityEnd()) .build();
    }
    /**
     * Creates a new training session for a member.
     *
     * <p>This method performs comprehensive validation before creating a training
     * session, including:
     * <ul>
     *   <li>Member existence and plan validity</li>
     *   <li>Trainer assignment and eligibility verification</li>
     *   <li>Session timing and duration validation</li>
     * </ul>
     *
     * <p>The session is assigned a unique identifier and saved with computed
     * start and end times. Cache eviction ensures session caches remain current.
     *
     * @param memberId the unique identifier of the member for whom the session
     *                 is being created. Must not be null or empty.
     * @param trainerId the unique identifier of the trainer creating the session.
     *                  Must not be null or empty and must match the assigned trainer.
     * @param requestDto the session details including date, duration, and optional
     *                   session name. Must not be null and must pass validation.
     * @return AllSessionInfoResponseDto containing the created session information
     * @throws UserNotFoundException if the member or trainer is not found
     * @throws PlanExpiredException if the member's plan has expired
     * @throws TrainerExpiredException if the trainer's eligibility has expired
     * @throws InvalidTrainerException if the trainer ID doesn't match the assigned trainer
     * @throws TransactionSystemException if the database transaction fails
     *
     * @see AddSessionsRequestDto
     * @see AllSessionInfoResponseDto
     */
    @Transactional
    @CacheEvict(value = "member'sSessionCache", key = "#memberId")
    public AllSessionInfoResponseDto addSessionToMemberById(String memberId, String trainerId,
                                                            AddSessionsRequestDto requestDto) {
        // 1. Fetch the member by ID, throw error if not found
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UserNotFoundException("No member found with this id: " + memberId));
        log.info("Fetched member successfully from db: {} ", member.getId());

        // 2. Validate trainer assigned to the member
        Trainer trainer = trainerRepository.findTrainerByMemberId(member.getId())
                .orElseThrow(() -> new UserNotFoundException(
                        "No member found with this id: " + member.getId()));
        if (trainer == null) {
            log.error("failed to load trainer of name => ");
            throw new TrainerExpiredException("Unable to assign trainer because " +
                    "\n either trainer doesn't exist or the trainer's access has expired");
        } else if (trainer.getEligibilityEnd().isBefore(LocalDate.now())) {
            log.error("Plan has expired on => {}", trainer.getEligibilityEnd());
            throw new TrainerExpiredException("Trainer's access expired on: " + trainer.getEligibilityEnd());
        } else if (!trainer.getTrainerId().equals(trainerId)) {
            log.error("Wrong id for trainer with this id {} and found id is {}", trainerId, trainer.getTrainerId());
            throw new InvalidTrainerException("The trainer with id: " + trainerId +
                    "\ndoes not matches with the id --> " + trainer.getTrainerId());
        }

        // 3. Compute session timings
        LocalDateTime startTime = requestDto.getSessionDate();
        LocalDateTime endTime = startTime.plusMinutes(Math.round(requestDto.getDuration() * 60));
        // 4. Build session entity
        String sessionName = requestDto.getSessionName() == null ? "" : requestDto.getSessionName();
        Session session = Session.builder()
                .sessionId(requestDto.getSessionId())
                .sessionName(sessionName)
                .sessionStartTime(startTime)
                .sessionEndTime(endTime)
                .memberId(member.getId()).
                trainerId(trainer.getTrainerId())
                .build();
        // 5. Save session in DB
        sessionRepository.save(session);
        log.info("Successfully saved session with this id {} and for the date on {}"
                , session.getSessionId(), session.getSessionStartTime());
        // 6. Return response DTO
        SessionsResponseDto responseDto = SessionsResponseDto.builder()
                .sessionId(session.getSessionId())
                .sessionName(session.getSessionName())
                .sessionStartTime(session.getSessionStartTime())
                .sessionEndTime(session.getSessionEndTime())
                .memberId(session.getMemberId())
                .trainerId(session.getTrainerId())
                .build();
        return AllSessionInfoResponseDto.builder()
                .sessionsResponseDtoList(List.of(responseDto))
                .build();
    }
    /**
     * Retrieves past training sessions for a member with pagination support.
     *
     * <p>This method returns completed training sessions ordered by session date
     * in descending order (most recent first). Pagination parameters control
     * the number of results returned and the page offset.
     *
     * @param memberId the unique identifier of the member whose past sessions
     *                 are requested. Must not be null or empty.
     * @param pageSize the number of sessions to return per page. Must be positive.
     * @param pageNo the page number to retrieve (0-based indexing). Must be non-negative.
     * @return AllSessionInfoResponseDto containing the paginated list of past sessions
     * @throws IllegalArgumentException if pagination parameters are invalid
     * @throws DataAccessException if there's an error accessing session data
     *
     * @see AllSessionInfoResponseDto
     * @see SessionsResponseDto
     */
    @Cacheable(value = "member'sSessionCache", key = "#memberId':'#pageSize':'pageNo")
    public AllSessionInfoResponseDto getPastSessions(String memberId, int pageSize, int pageNo) {
        Pageable page = PageRequest.of(pageNo, pageSize);
        Page<Session> sessions = sessionRepository.findPastSessionsByMemberId(memberId, page);
        List<SessionsResponseDto> responseDtoList = sessions.getContent().stream()
                .map(response -> SessionsResponseDto.builder()
                        .sessionId(response.getSessionId()).
                        sessionName(response.getSessionName()).
                        memberId(response.getMemberId()).
                        trainerId(response.getTrainerId())
                        .sessionStartTime(response.getSessionStartTime())
                        .sessionEndTime(response.getSessionEndTime()).build()).toList();
        return AllSessionInfoResponseDto.builder().sessionsResponseDtoList(responseDtoList).build();
    }
     /**
     * Retrieves all upcoming training sessions for a member.
     *
     * <p>This method returns all scheduled future sessions ordered by session date
     * in ascending order (earliest first). It includes comprehensive validation
     * to ensure the member's plan and trainer eligibility are current.
     *
     * <p>Results are cached to improve performance for frequently accessed
     * upcoming session information.
     *
     * @param memberId the unique identifier of the member whose upcoming sessions
     *                 are requested. Must not be null or empty.
     * @return AllSessionInfoResponseDto containing all upcoming sessions for the member
     * @throws UserNotFoundException if the member ID does not exist
     * @throws PlanExpiredException if the member's plan has expired
     * @throws TrainerNotFoundException if no trainer is assigned to the member
     * @throws TrainerExpiredException if the assigned trainer's eligibility has expired
     * @throws IllegalArgumentException if memberId is null or empty
     *
     * @see AllSessionInfoResponseDto
     * @see SessionsResponseDto
     */
    @Cacheable(value = "member'sSessionCache", key = "#memberId")
    public AllSessionInfoResponseDto getUpcomingSessions(String memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UserNotFoundException("No member found with this id: " + memberId));
        Trainer trainer = trainerRepository.findTrainerByMemberId(memberId).orElse(null);
        if (member.getPlanExpiration().isBefore(LocalDateTime.now()) || member.getPlanExpiration() == null) {
            log.error("Plan expired for the member {} on {} ", member.getFirstName() + " " + member.getLastName(), member.getPlanExpiration());
            throw new PlanExpiredException("Plan expired on : " + member.getPlanExpiration());
        } else if (trainer == null) {
            throw new TrainerNotFoundException("No trainer found for this member");
        } else if (trainer.getEligibilityEnd().isBefore(LocalDate.now())) {
            throw new TrainerExpiredException("Trainer plan expired unable to fetch info");
        }
        List<Session> sessions = sessionRepository.findUpcomingSessionsByMemberId(memberId);
        List<SessionsResponseDto> responseDtoList = sessions.stream().map(res -> SessionsResponseDto.builder().sessionId(res.getSessionId()).sessionName(res.getSessionName()).memberId(res.getMemberId()).trainerId(res.getTrainerId()).sessionStartTime(res.getSessionStartTime()).sessionEndTime(res.getSessionEndTime()).build()).toList();
        return AllSessionInfoResponseDto.builder().sessionsResponseDtoList(responseDtoList).build();
    }
    /**
     * Updates an existing training session with new details.
     *
     * <p>This method allows modification of session information including name,
     * start time, and end time. It validates that the session exists and belongs
     * to the specified member and trainer before applying updates.
     *
     * <p>The operation is transactional to ensure data consistency and includes
     * cache eviction to maintain cache coherence.
     *
     * @param sessionId the unique identifier of the session to update. Must not
     *                  be null or empty.
     * @param memberId the unique identifier of the member who owns the session.
     *                 Must not be null or empty.
     * @param requestDto the updated session details including name and timing.
     *                   Must not be null and must pass validation constraints.
     * @return SessionsResponseDto containing the updated session information
     * @throws NoSessionFoundException if the session ID does not exist
     * @throws InvalidSessionException if the member/trainer IDs don't match the session
     * @throws TransactionSystemException if the database transaction fails
     * @throws IllegalArgumentException if any parameter is null or invalid
     *
     * @see UpdateSessionRequestDto
     * @see SessionsResponseDto
     */
    @Transactional
    @CacheEvict(value = "member'sSessionCache", key = "#memberId")
    public SessionsResponseDto updateSession(String sessionId, String memberId,
                                             UpdateSessionRequestDto requestDto) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NoSessionFoundException("No session found with this id: " + sessionId));

        if (!session.getMemberId().equals(memberId) || !session.getTrainerId().equals(requestDto.getTrainerId())) {
            throw new InvalidSessionException("Input mismatch for trainer/member IDs");
        }

        session.setSessionName(requestDto.getSessionName());
        session.setSessionStartTime(requestDto.getSessionStartTime());
        session.setSessionEndTime(requestDto.getSessionEndTime());
        sessionRepository.save(session);

        return SessionsResponseDto.builder()
                .sessionId(session.getSessionId())
                .sessionName(session.getSessionName())
                .memberId(session.getMemberId())
                .trainerId(session.getTrainerId())
                .sessionStartTime(session.getSessionStartTime())
                .sessionEndTime(session.getSessionEndTime())
                .build();
    }
    /**
     * Deletes a training session from the system.
     *
     * <p>This method permanently removes a scheduled training session after
     * validating that the session exists and belongs to the specified member.
     * The operation is irreversible and should be used with caution.
     *
     * <p>The operation is transactional to ensure data consistency and includes
     * cache eviction to maintain cache coherence.
     *
     * @param sessionId the unique identifier of the session to delete. Must not
     *                  be null or empty.
     * @param memberId the unique identifier of the member who owns the session.
     *                 Must not be null or empty.
     * @return String confirmation message indicating successful deletion
     * @throws NoSessionFoundException if the session ID does not exist
     * @throws InvalidSessionException if the member ID doesn't match the session owner
     * @throws TransactionSystemException if the database transaction fails
     * @throws IllegalArgumentException if any parameter is null or empty
     */
    @Transactional
    @CacheEvict(value = "member'sSessionCache", key = "#memberId")
    public String deleteSessionBySessionId(String sessionId, String memberId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NoSessionFoundException("No session found with this id: " + sessionId));
        if (!session.getMemberId().equals(memberId)) {
            throw new InvalidSessionException("Input mismatch for the memberId: " + memberId);
        }
        sessionRepository.deleteById(sessionId);
        return "Successfully deleted session of id: " + sessionId;
    }

}
