package com.gym.trainerService.Services.MemberServices;

import com.gym.trainerService.Dto.MemberDtos.Responses.SessionMatrixInfo;
import com.gym.trainerService.Dto.SessionDtos.Requests.AddSessionRequestDto;
import com.gym.trainerService.Dto.SessionDtos.Requests.UpdateSessionRequestDto;
import com.gym.trainerService.Dto.SessionDtos.Responses.AllSessionResponseDto;
import com.gym.trainerService.Dto.SessionDtos.Wrappers.AllSessionsWrapperDto;

import com.gym.trainerService.Exception.Custom.*;
import com.gym.trainerService.Models.Member;
import com.gym.trainerService.Models.Session;
import com.gym.trainerService.Models.Trainer;
import com.gym.trainerService.Repositories.MemberRepository;
import com.gym.trainerService.Repositories.SessionRepository;
import com.gym.trainerService.Repositories.TrainerRepository;
import com.gym.trainerService.Services.OtherServices.WebClientService;
import com.gym.trainerService.Utils.CustomAnnotations.Annotations.LogRequestTime;
import com.gym.trainerService.Utils.CustomCacheEvict;
import com.gym.trainerService.Utils.SessionIdGenUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Service class managing business logic for handling training sessions between trainers and members.
 * <p>
 * This class coordinates with {@link TrainerRepository}, {@link MemberRepository}, and {@link SessionRepository}
 * to handle creation, updates, retrieval, and deletion of session records. It also communicates with the
 * {@link WebClientService} to notify other microservices about session-related events.
 * </p>
 *
 * <p><b>Transactional & Caching Strategy:</b></p>
 * <ul>
 *   <li>{@code @Transactional} – Ensures atomicity of DB operations within a method.</li>
 *   <li>{@code @Cacheable} – Caches read operations to improve performance and reduce DB hits.</li>
 *   <li>{@code @CachePut} – Updates cache after a create or update operation to keep data consistent.</li>
 *   <li>{@code @CacheEvict} – Removes cache entries after deletion to prevent stale data.</li>
 * </ul>
 *
 * <p><b>Author:</b> Arpan Das</p>
 * @since 1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SessionManagementService {
    // injecting MemberRepository by constructor injection using @RequiredArgsConstructor
    private final MemberRepository memberRepository;
    // injecting TrainerRepository by constructor injection using @RequiredArgsConstructor
    private final TrainerRepository trainerRepository;
    // injecting SessionRepository by constructor injection using @RequiredArgsConstructor
    private final SessionRepository sessionRepository;
    // injecting WebClientService by constructor injection using @RequiredArgsConstructor
    private final WebClientService webClientService;
    // injecting SessionIdGenUtil by constructor injection using @RequiredArgsConstructor
    private final SessionIdGenUtil sessionIdGenUtil;

    private final CustomCacheEvict customCacheEvict;
    private final CacheManager cacheManager;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Creates and schedules a new session for a trainer with an assigned member.
     * <p>
     * Performs multiple validations before persisting the session:
     * <ul>
     *   <li>Ensures trainer exists in DB.</li>
     *   <li>Verifies member eligibility and plan validity.</li>
     *   <li>Checks for overlapping session slots.</li>
     *   <li>Notifies the Member service once session is successfully created.</li>
     * </ul>
     *
     * <p>Cache is updated using {@code @CachePut} to maintain consistency with new session data.</p>
     *
     * @param trainerId  unique identifier of the trainer
     * @param requestDto DTO containing session creation details
     * @return {@link AllSessionsWrapperDto} containing updated list of sessions for the trainer
     */
    @Transactional
    @LogRequestTime
    @Caching(evict = {
            @CacheEvict(value = "DashboardInfo",key = "#trainerId"),
            @CacheEvict(value = "sessionMatrix",key = "#trainerId")
    })
    public String addSession(String trainerId, String status, AddSessionRequestDto requestDto) {
        // validating trainer existence if not present then throw a custom exception */
        if(!trainerRepository.existsById(trainerId)) {
            log.warn("No trainer found for the trainer with the id ---> {}",trainerId);
            throw new NoTrainerFoundException("No trainer found with this id :: "+trainerId);
        }
        Member member = memberRepository.findByTrainerIdMemberId(trainerId,requestDto.getMemberId())
                .orElseThrow(()-> new MemberNotFoundException("No member found for this credentials"));
        log.info("fetched member :: {} from database ",member.getMemberName());
        // validating member eligibility by checking plan expiration if not valid then throw exception */
        if(member.getEligibilityEnd().isBefore(requestDto.getSessionDate().toLocalDate())) {
            log.warn("Unable to add new session as the plan expired for the member --> {} on :: {}",
                    member.getMemberName(), member.getEligibilityEnd());
            throw new PlanExpirationException("Unable to add sessions due to plan expired");
        }
        // calculating the start and end time */
        LocalDateTime startTime = requestDto.getSessionDate();
        LocalDateTime endTime = startTime.plusMinutes(Math.round(requestDto.getDuration() * 60));
        /* checking for overlapping sessions validating session time slots by checking overlapping sessions
         * if overlapping found then throw a custom exception
         */
        if(sessionRepository.sessionSlotCheck(startTime,endTime).isPresent()) {
            log.warn("Wrong slot, no empty slot found between {} and {} "
                    ,startTime.format(formatter),endTime.format(formatter));
            throw new InvalidSessionException("No empty slot is available between "+startTime.format(formatter) +
                    " & "+ endTime.format(formatter));
        }
        // generating session ID by using sessionIdGenUtil */
        String sessionId = sessionIdGenUtil
                .generateSessionId(requestDto.getMemberId(), member.getTrainerId(),startTime,endTime);
                log.info("Generated session id :: {} for the member {}",sessionId,member.getMemberId());
        Session session = Session.builder()
                .sessionId(sessionId)
                .sessionName(requestDto.getSessionName())
                .memberId(member.getMemberId())
                .trainerId(member.getTrainerId())
                .sessionStartTime(startTime)
                .sessionEndTime(endTime)
                .status(status)
                .build();
        // saving session to database */
        log.info("Successfully saved session on {}",session.getSessionStartTime().format(formatter));
        Mono<String> webClientResponse = webClientService.sendSessionToMember(session,requestDto.getDuration());

        try {
            String response;
            response = webClientResponse
                    .doOnSuccess(msg -> {
                        sessionRepository.save(session);
                        customCacheEvict.evictTrainerSessionCachePattern("AllSessionCache",trainerId,"UP");
                        log.info("Successfully saved session to database after Member Service confirmation on {}",
                                session.getSessionStartTime().format(formatter));
                    })
                    .block();

            return response;
        } catch (RuntimeException e) {
            log.error("Failed to add session after WebClient call. Session not saved locally.", e);
            throw e;
        }
    }

    /**
     * Updates details of an existing session.
     * <p>
     * Performs the following checks:
     * <ul>
     *   <li>Validates that the session exists.</li>
     *   <li>Ensures the provided trainer and member IDs match the session’s ownership.</li>
     *   <li>Checks for slot conflicts with other sessions.</li>
     *   <li>Updates session timings and notifies Member service.</li>
     * </ul>
     *
     * <p>Cache entry for the corresponding trainer is updated via {@code @CachePut}.</p>
     *
     * @param sessionId  unique identifier of the session
     * @param requestDto DTO containing updated session information
     * @return updated {@link AllSessionsWrapperDto} for the trainer
     */
    @Transactional
    @LogRequestTime
    @Caching(evict = {
            @CacheEvict(value = "DashboardInfo",key = "#trainerId"),
            @CacheEvict(value = "sessionMatrix",key = "#trainerId")
    })
    public String  updateSession(String sessionId, UpdateSessionRequestDto requestDto) {
        // fetching trainer to check authenticity and if he/she is permissible to do so
        Trainer trainer = getById(requestDto.getTrainerId());
        // fetching session from database if not found then throw custom exception
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(()-> new NoSessionFoundException("No session found with this id: "+sessionId));
        log.info("Successfully retrieved session of id: {} from database",session.getSessionId());
        // validating session with trainerId and memberId if not matches then throw exception */
        if(! session.getTrainerId().equals(trainer.getTrainerId()) ||
        !session.getMemberId().equals(requestDto.getMemberId())) {
            log.warn("Invalid credentials for trainer or member id");
            throw new InvalidSessionException("session does not matches with trainer or member");
        }
        // calculating the start and end time */
        LocalDateTime startTime = requestDto.getSessionDate();
        LocalDateTime endTime = startTime.plusMinutes(Math.round(requestDto.getDuration() * 60));
        Optional<Session> slotCheck = sessionRepository.sessionSlotCheck(startTime,endTime);
        // checking for overlapping sessions if overlapping found with different session id then throw exception */
        if (slotCheck.isPresent()) {
            Session sessionCheck = slotCheck.get();

            // Check if the overlapping session found has a DIFFERENT ID than the session being updated.
            // If it's the same session, it's just a time update on the current session, which is fine.
            if (!sessionCheck.getSessionId().equals(sessionId)) {
                log.warn("Wrong slot, overlapping session found (ID: {}) between {} and {} ",
                        sessionCheck.getSessionId(), startTime.format(formatter), endTime.format(formatter));
                // The original requirement: "already u have a session in that window"
                throw new InvalidSessionException(
                        "Another session is already scheduled during the requested slot (" +
                                sessionCheck.getSessionName() +
                                " from " + sessionCheck.getSessionStartTime().format(formatter) +
                                " to " + sessionCheck.getSessionEndTime().format(formatter) +
                                ")"
                );
            }
            // If the sessionCheck ID matches the current sessionId, we proceed with the update.
        }
        // updating session details */
        session.setSessionName(requestDto.getSessionName());
        session.setSessionStartTime(startTime);
        session.setSessionEndTime(endTime);
        log.info("Session updated with name {} between {} - {}", session.getSessionName(),
                session.getSessionStartTime().format(formatter),session.getSessionEndTime().format(formatter));
        // sending session details to member service by webClientService */

        log.info("Dto send to webClient service for further process");
        customCacheEvict.evictTrainerSessionCachePattern("AllSessionCache",session.getTrainerId(),"UP");
        Mono<String> webClientResponse = webClientService.updateSessionToMember(session);
        try {
            String response;
            response = webClientResponse
                    .doOnSuccess(msg -> {
                        sessionRepository.save(session);
                        customCacheEvict
                                .evictTrainerSessionCachePattern("AllSessionCache",session.getTrainerId(),"UP");
                        log.info("Successfully Updated session to database after Member Service confirmation on {}",
                                session.getSessionStartTime().format(formatter));
                    })
                    .block();

            return response;
        } catch (RuntimeException e) {
            log.error("Failed to add session after WebClient call. Session not saved locally.", e);
            throw e;
        }
    }

    /**
     * Retrieves all upcoming sessions for a given trainer.
     * <p>
     * Uses {@code @Cacheable} to cache the result in Redis to optimize future reads.
     * Cache key is based on {@code trainerId}.
     * </p>
     *
     * @param trainerId unique identifier of the trainer
     * @return {@link AllSessionsWrapperDto} containing all upcoming sessions
     */
    @LogRequestTime
    @Cacheable(value = "AllSessionCache", key = "'UP:' + #trainerId + ':' + #pageNo + ':' + #pageSize")
    public AllSessionsWrapperDto getUpcomingSessions(String trainerId, int pageNo, int pageSize) {
        log.info("Request received in service class for all upcoming sessions for trainer {}", trainerId);
        getById(trainerId);
        Sort sort = Sort.by(Sort.Direction.ASC, "sessionStartTime");
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<Session> sessionPage = sessionRepository.findAllUpcoming(LocalDateTime.now(), pageable);
        log.info("");
        return AllSessionsWrapperDto.builder()
                .responseDtoList(sessionPage.stream()
                        .map(s -> AllSessionResponseDto.builder()
                                .sessionId(s.getSessionId())
                                .memberId(s.getMemberId())
                                .sessionStatus(s.getStatus())
                                .sessionName(s.getSessionName())
                                .sessionStartTime(s.getSessionStartTime())
                                .sessionEndTime(s.getSessionEndTime())
                                .build()).toList())
                .pageNo(sessionPage.getNumber())
                .pageSize(sessionPage.getSize())
                .totalElements(sessionPage.getTotalElements())
                .totalPages(sessionPage.getTotalPages())
                .lastPage(sessionPage.isLast())
                .build();
    }
    /**
     * Retrieves past sessions for a given trainer using pagination.
     * <p>
     * Cache key combines {@code trainerId}, {@code pageNo}, and {@code pageSize}
     * to uniquely identify paginated results in the cache.
     * </p>
     *
     * @param trainerId unique identifier of the trainer
     * @param pageNo    current page number
     * @param pageSize  number of records per page
     * @return {@link AllSessionsWrapperDto} containing paginated session data
     */
    @LogRequestTime
    @Cacheable(value = "AllSessionCache",
            key = "'PAST:' + #trainerId + ':' + #pageNo + ':' + #pageSize + ':' + #sortDirection")
    public AllSessionsWrapperDto getPastSessionsByPagination(String trainerId, int pageNo, int pageSize,String sortDirection)
    {
        log.info("Request received in service class for past sessions for size {} and page no {}",pageSize,pageNo);
        Trainer trainer;
        try {
            trainer = (Trainer) Objects.requireNonNull(cacheManager.getCache("trainer")).get(trainerId);
        } catch (Exception e) {
            log.warn("Got an exception due to {}",e.getLocalizedMessage());
            trainer = trainerRepository.findById(trainerId)
                    .orElseThrow(() -> new NoTrainerFoundException(
                            "No trainer found with the id: " + trainerId));
        }
        if (trainer != null && trainer.isFrozen()) {
            throw new UnAuthorizedRequestException("Your Account has been Suspended Please Contact Admin");
        }

        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction,"sessionStartTime");
        Pageable pageRequest = PageRequest.of(pageNo,pageSize,sort);
        // fetching paginated data from database */
        Page<Session> sessionPage = sessionRepository
                .findPaginatedDataByTrainerId(trainerId,LocalDateTime.now(),pageRequest);
        log.info("Successfully retrieved {} sessions for the trainer id:: {}",sessionPage.getSize(),trainerId);
        return AllSessionsWrapperDto.builder()
                .responseDtoList(sessionPage.stream()
                        .map(s -> AllSessionResponseDto.builder()
                                .sessionId(s.getSessionId())
                                .memberId(s.getMemberId())
                                .sessionStatus(s.getStatus())
                                .sessionName(s.getSessionName())
                                .sessionStartTime(s.getSessionStartTime())
                                .sessionEndTime(s.getSessionEndTime())
                                .build()).toList())
                .pageNo(sessionPage.getNumber())
                .pageSize(sessionPage.getSize())
                .totalElements(sessionPage.getTotalElements())
                .totalPages(sessionPage.getTotalPages())
                .lastPage(sessionPage.isLast())
                .build();
    }
    /**
     * Deletes an existing session for a trainer.
     * <p>
     * <b>Cache Eviction Strategy:</b>
     * <ul>
     *   <li>Removes cache entry specific to the trainer.</li>
     *   <li>Clears wildcard entries (e.g., paginated session caches) to maintain consistency.</li>
     * </ul>
     *
     * @param sessionId ID of the session to be deleted
     * @param trainerId ID of the trainer performing the deletion
     * @return success message after deletion
     */
    @LogRequestTime
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "DashboardInfo",key = "#trainerId"),
            @CacheEvict(value = "sessionMatrix",key = "#trainerId")
    })
    public String deleteSession(String sessionId,String trainerId) {
        // fetching session from database if not found then throw custom exception */
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(()-> new NoSessionFoundException("No session found with this id: "+sessionId));
        log.info("Successfully retrieved session of id: {} from database to delete",session.getSessionId());
        // validating session with trainerId if not matches then throw exception */
        if(!session.getTrainerId().equals(trainerId)) {
            log.warn("Session and trainer id mismatch");
            throw new InvalidSessionException("Invalid session with trainerId");
        }
        // deleting session from database */
        sessionRepository.deleteById(session.getSessionId());
        log.info("Successfully deleted session on {}",session.getSessionStartTime().format(formatter));
        // sending delete request to member service by webClientService */
        Mono<String> webClientResponse =  webClientService
                .deleteSessionForMember(session.getSessionId(),session.getMemberId());
        try {
            String response;
            response = webClientResponse
                    .doOnSuccess(msg -> {
                        sessionRepository.save(session);
                        customCacheEvict
                                .evictTrainerSessionCachePattern("AllSessionCache",session.getTrainerId(),"UP");
                        log.info("Successfully deleted session to database after Member Service confirmation on {}",
                                session.getSessionStartTime().format(formatter));
                    })
                    .block();

            return response;
        } catch (RuntimeException e) {
            log.error("Failed to add session after WebClient call. Session not saved locally.", e);
            throw e;
        }
    }

    @LogRequestTime
    @Cacheable(value = "sessionMatrix",key = "#trainerId")
    public SessionMatrixInfo getSessionMatrix(String trainerId) {
        log.info("Starting session matrix calculation for trainer: {}", trainerId);

        LocalDateTime today = LocalDateTime.now();
        LocalDateTime startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDateTime endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
        LocalDateTime now = LocalDateTime.now();

        log.debug("Current date: {}, Week range: {} to {}", today, startOfWeek, endOfWeek);

        List<Session> sessions = sessionRepository.sessionInWeekRange(startOfWeek, endOfWeek);

        log.info("Repository returned {} total sessions for the week.", sessions.size());

        int totalSessions = sessions.size();

        long remainingSessionsLong = sessions.stream()
                .filter(s -> s.getSessionEndTime().isAfter(now))
                .count();

        int remainingSessions = (int) remainingSessionsLong;
        log.debug("Calculated remaining sessions: {}", remainingSessions);

        SessionMatrixInfo result = SessionMatrixInfo.builder()
                .totalSessionsThisWeek(totalSessions)
                .totalSessionsLeft(remainingSessions)
                .build();

        log.info("Finished session matrix calculation for trainer {}. Result: Total={}, Remaining={}", trainerId, totalSessions, remainingSessions);

        return result;
    }

    private Trainer getById(String trainerId){
        Trainer trainer = null;

        Cache.ValueWrapper wrapper = Objects.requireNonNull(cacheManager.getCache("trainer")).get(trainerId);

        if (wrapper != null) {
            try {
                trainer = (Trainer) wrapper.get();
                log.info("Trainer {} {} found in cache", Objects.requireNonNull(trainer).getFirstName(), trainer.getLastName());
            } catch (ClassCastException e) {
                log.warn("Error occurred during cache cast for trainerId {}: {}", trainerId, e.getLocalizedMessage());
            }
        }

        if (trainer == null) {
            trainer = trainerRepository.findById(trainerId)
                    .orElseThrow(()-> new NoTrainerFoundException("No Trainer Found with this id"));

            log.info("Reloaded trainer {} {} from db", trainer.getFirstName(), trainer.getLastName());
            Objects.requireNonNull(cacheManager.getCache("trainer")).put(trainer.getTrainerId(), trainer);
        }

        if(trainer.isFrozen()){
            throw new UnAuthorizedRequestException("Your Account has been Suspended Please Contact Admin");
        }

        log.info("Session service returning {} {} by private method", trainer.getFirstName(), trainer.getLastName());
        return trainer;
    }

    /**
     * This method is responsible to set a status for a session
     * @param sessionId to fetch a valid {@link Session} from db
     * @param trainerId to validate the authority of trainer by
     * @see SessionManagementService#getById(String trainerId)
     * @param status to set the status of the session
     *               evicts also the cache on the basis of session's time
     * @see SessionManagementService#evictIt(Session)
     * @return Success Message retrieved from member service
     */
    @Transactional
    @LogRequestTime
    public String setStatusForSession(String sessionId, String trainerId, String status) {
        log.info("Request received in service class to update status for session {}", sessionId);
        Trainer trainer = getById(trainerId);
        log.info("Fetched details for --> {} {}", trainer.getFirstName(), trainer.getLastName());
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NoSessionFoundException("No Session Found With this Session credentials"));
        session.setStatus(status.toUpperCase());
        Mono<String> webClientResponse = webClientService.updateSessionStatusForMember
                (session.getSessionId(), session.getMemberId(), session.getTrainerId(), status);
        try {
            String response;
            response = webClientResponse
                    .doOnSuccess(msg -> {
                        evictIt(session);
                        sessionRepository.save(session);
                        log.info("Successfully updated session to database after Member Service confirmation on {}",
                                session.getSessionStartTime().format(formatter));
                    }).block();
            return response;
        } catch (Exception exception) {
            log.error("💀💀 Failed to update session status after WebClient call", exception);
            throw exception;
        }
    }

    /**
     * a private method to evict cache for {@link Trainer} from cache
     * @param session to validate the session is in the past or future
     * @see Session
     * @see Trainer
     * also see the method how the custom eviction for cache works
     * @see CustomCacheEvict#evictTrainerSessionCachePattern(String, String, String)
     */
    private void evictIt(Session session) {
        log.info("Request received for evict session cache");
        boolean isPresentOrFuture = session.getSessionStartTime().isAfter(LocalDateTime.now());

        if (isPresentOrFuture) {
            customCacheEvict
                    .evictTrainerSessionCachePattern("AllSessionCache", session.getTrainerId(), "UP");
        } else {
            customCacheEvict
                    .evictTrainerSessionCachePattern("AllSessionCache", session.getTrainerId(), "PAST");
        }
    }
}
