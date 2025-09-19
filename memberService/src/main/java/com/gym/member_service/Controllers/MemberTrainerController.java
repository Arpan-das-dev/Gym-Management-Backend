package com.gym.member_service.Controllers;

import com.gym.member_service.Dto.MemberTrainerDtos.Requests.AddSessionsRequestDto;
import com.gym.member_service.Dto.MemberTrainerDtos.Requests.AddTrainerRequestDto;
import com.gym.member_service.Dto.MemberTrainerDtos.Requests.TrainerAssignRequestDto;
import com.gym.member_service.Dto.MemberTrainerDtos.Requests.UpdateSessionRequestDto;
import com.gym.member_service.Dto.MemberTrainerDtos.Responses.SessionsResponseDto;
import com.gym.member_service.Dto.MemberTrainerDtos.Responses.TrainerAssignResponseDto;
import com.gym.member_service.Dto.MemberTrainerDtos.Responses.TrainerInfoResponseDto;
import com.gym.member_service.Dto.MemberTrainerDtos.Wrapper.AllSessionInfoResponseDto;
import com.gym.member_service.Services.FeatureServices.MemberTrainerService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for managing member-trainer relationships and training sessions.
 *
 * <p>This controller provides endpoints for:
 * <ul>
 *   <li>Requesting trainer assignments from administrators</li>
 *   <li>Assigning trainers to members</li>
 *   <li>Retrieving trainer information</li>
 *   <li>Managing training sessions (create, read, update, delete)</li>
 * </ul>
 *
 * <p>All endpoints are mapped under the base URL configured by the property
 * {@code member-service.Base_Url.Trainer}.
 *
 * <p>This controller uses validation annotations to ensure request data integrity
 * and follows RESTFull conventions for HTTP status codes and resource naming.
 *
 * @author Arpan Das
 * @version 1.0
 * @since 1.0
 */

@Slf4j
@RestController
@RequestMapping("${member-service.Base_Url.Trainer}")
@RequiredArgsConstructor
@Validated
public class MemberTrainerController {
    /**
     * Service layer dependency for handling member-trainer business logic.
     */
    private final MemberTrainerService trainerService;
    /**
     * Sends a trainer assignment request to the administrator.
     *
     * <p>This endpoint allows members to request a trainer assignment through
     * the administrative system. The request is processed asynchronously and
     * returns an acknowledgment response.
     *
     * @param requestDto the trainer assignment request containing member details
     *                   and trainer preferences. Must not be null and must pass
     *                   validation constraints.
     * @return ResponseEntity containing the trainer assignment response with
     *         HTTP status 202 (ACCEPTED) indicating the request has been queued
     *         for processing
     * @throws ValidationException if the request DTO fails validation
     * @throws IllegalArgumentException if the member ID is invalid
     *
     * @see TrainerAssignRequestDto
     * @see TrainerAssignResponseDto
     */
    @PostMapping("/request")
    public ResponseEntity<TrainerAssignResponseDto> sendTrainerRequestToAdmin(@RequestBody @Valid TrainerAssignRequestDto requestDto) {
        log.info("Request received to send admin service by member id: {}", requestDto.getMemberId());
        TrainerAssignResponseDto responseDto = trainerService.requestAdminForTrainer(requestDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(responseDto);
    }
    /**
     * Assigns a trainer to a member.
     *
     * <p>This endpoint is typically used by administrators to complete trainer
     * assignments. It creates the trainer-member relationship and returns the
     * trainer's information.
     *
     * @param requestDto the trainer assignment request containing trainer and
     *                   member details. Must not be null and must pass validation
     *                   constraints.
     * @return ResponseEntity containing the assigned trainer's information with
     *         HTTP status 202 (ACCEPTED) indicating successful assignment
     * @throws ValidationException if the request DTO fails validation
     * @throws EntityNotFoundException if the trainer or member is not found
     * @throws IllegalStateException if the member already has an active trainer
     *
     * @see AddTrainerRequestDto
     * @see TrainerInfoResponseDto
     */
    @PostMapping("/addTrainer")
    public ResponseEntity<TrainerInfoResponseDto> assignTrainerForMember(@RequestBody @Valid AddTrainerRequestDto requestDto) {
        log.info("Request received in the controller to assign trainer {}", requestDto.getTrainerName());
        TrainerInfoResponseDto response = trainerService.assignTrainerToMember(requestDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
    /**
     * Retrieves trainer information for a specific member.
     *
     * <p>This endpoint returns detailed information about the trainer currently
     * assigned to the specified member, including trainer credentials, specializations,
     * and contact information.
     *
     * @param memberId the unique identifier of the member whose trainer information
     *                 is requested. Must not be null or empty.
     * @return ResponseEntity containing the trainer information with HTTP status
     *         200 (OK) if the trainer is found
     * @throws IllegalArgumentException if memberId is null or empty
     * @throws EntityNotFoundException if no trainer is assigned to the member
     *
     * @see TrainerInfoResponseDto
     */
    @GetMapping("/getTrainer")
    public ResponseEntity<TrainerInfoResponseDto> getTrainerInfoByMemberId(@RequestParam String memberId) {
        log.info("Request received to get trainer info by member by id: {}", memberId);
        TrainerInfoResponseDto response = trainerService.getTrainerInfo(memberId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    /**
     * Adds a new training session for a member.
     *
     * <p>This endpoint allows trainers to schedule new training sessions for their
     * assigned members. The session details include timing, exercises, and objectives.
     *
     * @param memberId the unique identifier of the member for whom the session
     *                 is being added. Must not be null or empty.
     * @param trainerId the unique identifier of the trainer adding the session.
     *                  Must not be null or empty.
     * @param requestDto the session details including date, time, duration, and
     *                   exercise plan. Must not be null and must pass validation
     *                   constraints.
     * @return ResponseEntity containing all session information with HTTP status
     *         200 (OK) indicating successful session creation
     * @throws ValidationException if the request DTO fails validation
     * @throws EntityNotFoundException if the member or trainer is not found
     * @throws IllegalArgumentException if the trainer is not assigned to the member
     *
     * @see AddSessionsRequestDto
     * @see AllSessionInfoResponseDto
     */
    @PostMapping("/addSession")
    public ResponseEntity<AllSessionInfoResponseDto> addSessionToMember(@RequestParam String memberId,
                                                                        @RequestParam String trainerId,
                                                                        @Valid @RequestBody AddSessionsRequestDto
                                                                                    requestDto) {
        log.info("Request received to add session to member {} by trainer {}", memberId, trainerId);
        AllSessionInfoResponseDto response = trainerService.addSessionToMemberById(memberId, trainerId, requestDto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    /**
     * Retrieves past training sessions for a member with pagination.
     *
     * <p>This endpoint returns a paginated list of completed training sessions
     * for the specified member, ordered by session date in descending order
     * (most recent first).
     *
     * @param memberId the unique identifier of the member whose past sessions
     *                 are requested. Must not be null or empty.
     * @param pageSize the number of sessions to return per page. Must be positive.
     * @param pageNo the page number to retrieve (0-based). Must be positive.
     * @return ResponseEntity containing the paginated session information with
     *         HTTP status 202 (ACCEPTED)
     * @throws ValidationException if pageSize or pageNo are not positive
     * @throws IllegalArgumentException if memberId is null or empty
     * @throws EntityNotFoundException if the member is not found
     *
     * @see AllSessionInfoResponseDto
     */
    @GetMapping("/sessions/past")
    public ResponseEntity<AllSessionInfoResponseDto> getPastSessions(@RequestParam @NotBlank String memberId,
                                                                     @RequestParam @Positive int pageSize,
                                                                     @RequestParam @Positive int pageNo)
    {
        log.info("Request received to get past sessions of page size {} and page no {}", pageSize, pageNo);
        AllSessionInfoResponseDto response = trainerService.getPastSessions(memberId, pageSize, pageNo);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
    /**
     * Retrieves upcoming training sessions for a member.
     *
     * <p>This endpoint returns all scheduled future training sessions for the
     * specified member, ordered by session date in ascending order (earliest first).
     *
     * @param memberId the unique identifier of the member whose upcoming sessions
     *                 are requested. Must not be null or empty.
     * @return ResponseEntity containing all upcoming session information with
     *         HTTP status 202 (ACCEPTED)
     * @throws ValidationException if memberId is blank
     * @throws EntityNotFoundException if the member is not found
     *
     * @see AllSessionInfoResponseDto
     */
    @GetMapping("/sessions/next")
    public ResponseEntity<AllSessionInfoResponseDto> getUpcomingSessions(@RequestParam @NotBlank String memberId) {
        log.info("Request received to get upcoming sessions for memberId {}", memberId);
        AllSessionInfoResponseDto response = trainerService.getUpcomingSessions(memberId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
    /**
     * Updates an existing training session.
     *
     * <p>This endpoint allows modification of session details such as timing,
     * exercises, or objectives. Only future sessions can be updated to maintain
     * historical accuracy.
     *
     * @param sessionId the unique identifier of the session to update. Must not
     *                  be null or empty.
     * @param memberId the unique identifier of the member who owns the session.
     *                 Must not be null or empty.
     * @param requestDto the updated session details. Must not be null and must
     *                   pass validation constraints.
     * @return ResponseEntity containing the updated session information with
     *         HTTP status 201 (CREATED) indicating successful update
     * @throws ValidationException if the request DTO fails validation or if
     *                           sessionId/memberId are blank
     * @throws EntityNotFoundException if the session or member is not found
     * @throws IllegalStateException if attempting to update a past session
     *
     * @see UpdateSessionRequestDto
     * @see SessionsResponseDto
     */
    @PutMapping("/update-session")
    public ResponseEntity<SessionsResponseDto> updateSessionById(@RequestParam @NotBlank String sessionId,
                                                                 @RequestParam @NotBlank String memberId,
                                                                 @Valid @RequestBody UpdateSessionRequestDto
                                                                         requestDto)
    {
        log.info("Request received to update session with id {}", sessionId);
        SessionsResponseDto response = trainerService.updateSession(sessionId,memberId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    /**
     * Deletes a training session.
     *
     * <p>This endpoint removes a scheduled training session from the system.
     * Only future sessions can be deleted to maintain historical records.
     * The operation is irreversible.
     *
     * @param sessionId the unique identifier of the session to delete. Must not
     *                  be null or empty.
     * @param memberId the unique identifier of the member who owns the session.
     *                 Must not be null or empty.
     * @return ResponseEntity containing a confirmation message with HTTP status
     *         200 (OK) indicating successful deletion
     * @throws ValidationException if sessionId or memberId are blank
     * @throws EntityNotFoundException if the session or member is not found
     * @throws IllegalStateException if attempting to delete a past session
     */
    @DeleteMapping("/session")
    public ResponseEntity<String> deleteSessionByIds(@RequestParam @NotBlank String sessionId,
                                                     @RequestParam @NotBlank String memberId) {
        log.info("Request received to delete session of id: {} for memberId: {}", sessionId, memberId);
        String response = trainerService.deleteSessionBySessionId(sessionId, memberId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
