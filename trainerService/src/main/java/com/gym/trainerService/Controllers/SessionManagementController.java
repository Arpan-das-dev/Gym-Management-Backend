package com.gym.trainerService.Controllers;

import com.gym.trainerService.Dto.MemberDtos.Responses.GenericResponse;
import com.gym.trainerService.Dto.SessionDtos.Requests.AddSessionRequestDto;
import com.gym.trainerService.Dto.SessionDtos.Requests.UpdateSessionRequestDto;
import com.gym.trainerService.Dto.SessionDtos.Wrappers.AllSessionsWrapperDto;
import com.gym.trainerService.Services.MemberServices.SessionManagementService;
import com.gym.trainerService.Utils.CustomAnnotations.Annotations.LogExecutionTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing trainer session operations such as creation,
 * update, retrieval, and deletion.
 * <p>
 * This controller acts as the entry point for HTTP requests related to session management
 * in the Trainer Service. It delegates all business logic to the {@link SessionManagementService}.
 * </p>
 *
 * <p><b>Annotations used:</b></p>
 * <ul>
 *   <li>{@code @RestController} → Combines {@code @Controller} and {@code @ResponseBody}, simplifying REST API creation.</li>
 *   <li>{@code @RequestMapping} → Defines the base URL for all endpoints in this controller, configured from application properties.</li>
 *   <li>{@code @Slf4j} → Provides a logging utility using Lombok for structured request/response tracking.</li>
 *   <li>{@code @RequiredArgsConstructor} → Generates a constructor for all {@code final} fields, ensuring constructor-based dependency injection.</li>
 *   <li>{@code @Validated} → Enables method-level validation for incoming request parameters and DTOs.</li>
 * </ul>
 *
 * <p><b>Base URL:</b> Configured as {@code ${trainer-service.Base_Url.sessionManagement}}</p>
 *
 * <p><b>Author:</b> Arpan</p>
 * @since 1.0
 */
@RestController
@RequestMapping("${trainer-service.Base_Url.sessionManagement}")
@Slf4j
@RequiredArgsConstructor
@Validated
public class SessionManagementController {

    /* injecting sessionManagementService by using constructor injection */
    private final SessionManagementService sessionManagementService;

    /**
     * Creates a new training session for a given trainer and member.
     * <p>
     * Validates the incoming {@link AddSessionRequestDto} and delegates
     * the session creation logic to the {@link SessionManagementService}.
     * </p>
     *
     * @param trainerId  unique identifier of the trainer
     * @param requestDto request body containing session details such as memberId, date, and duration
     * @return {@link ResponseEntity} containing {@link AllSessionsWrapperDto} with updated session list
     * @status 201 CREATED if the session is successfully created
     */
    @PostMapping("/trainer/addSessions")
    public ResponseEntity<GenericResponse> addSession(@RequestParam String trainerId,
                                                      @RequestParam(defaultValue = "UPCOMING") String  status,
                                                      @Valid @RequestBody
                                                            AddSessionRequestDto requestDto){
        log.info("Request received to add session for member {} with trainer {}"
                ,requestDto.getMemberId(),trainerId);
       String  response = sessionManagementService.addSession(trainerId,status,requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new GenericResponse(response));
    }

    /**
     * Updates an existing training session’s details.
     * <p>
     * Accepts a {@link UpdateSessionRequestDto} to modify attributes such as time, duration, or member notes.
     * </p>
     *
     * @param sessionId  unique identifier of the session to be updated
     * @param requestDto DTO containing updated session details
     * @return {@link ResponseEntity} with {@link AllSessionsWrapperDto} representing the updated session list
     * @status 202 ACCEPTED if the update is successfully processed
     */
    @LogExecutionTime
    @PutMapping("/trainer/updateSession")
    public ResponseEntity<GenericResponse> updateSession(@RequestParam String sessionId,
                                                               @Valid @RequestBody
                                                               UpdateSessionRequestDto requestDto) {
        log.info("Request received for update session of id: {}",sessionId);
        String  response = sessionManagementService.updateSession(sessionId,requestDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new GenericResponse(response));
    }

    /**
     * Retrieves all upcoming sessions for a specific trainer.
     * <p>
     * Uses caching (via {@code @Cacheable}) in the service layer to improve performance
     * by reducing redundant database queries.
     * </p>
     *
     * @param trainerId unique identifier of the trainer
     * @return {@link ResponseEntity} containing {@link AllSessionsWrapperDto} with future sessions
     * @status 200 OK if sessions are found
     */
    @GetMapping("/trainer/getSessions")
    public ResponseEntity<AllSessionsWrapperDto> getUpcomingSessions (
            @RequestParam @NotBlank(message = "Can Not Proceed Request Without a Valid Trainer Id") String trainerId,
            @RequestParam @PositiveOrZero(message = "Page No Can not be Negative") int pageNo,
            @RequestParam @Positive(message = "Page size Must be Greater than Zero") int pageSize) {
        log.info("Request received to get upcoming sessions for trainer: {} for page no {} of size {}",
                trainerId,pageNo,pageSize);
        AllSessionsWrapperDto response = sessionManagementService.getUpcomingSessions(trainerId,pageNo,pageSize);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Retrieves paginated past sessions for a specific trainer.
     * <p>
     * Allows specifying {@code pageNo} and {@code pageSize} for flexible pagination.
     * Ensures both parameters are positive using validation annotations.
     * </p>
     *
     * @param pageSize  number of records per page ---> default 20
     * @param trainerId unique identifier of the trainer
     * @param pageNo    page number (starting from 0 or 1 depending on frontend convention)
     * @return {@link ResponseEntity} containing a paginated {@link AllSessionsWrapperDto}
     * @status 200 OK if sessions are retrieved successfully
     */
    @GetMapping("/trainer/getSession/{pageSize}")
    public ResponseEntity<AllSessionsWrapperDto> getPastSessions(
            @PathVariable @Positive int pageSize,
            @RequestParam @NotBlank(message = "Can Not Proceed Request Without a Valid Trainer Id") String trainerId,
            @RequestParam @PositiveOrZero int pageNo,
            @RequestParam(defaultValue = "ASC") @NotBlank String sortDirection) {
        log.info("Request received to get past sessions for pageNo: {}, of size: {}", pageNo, pageSize);
        AllSessionsWrapperDto response = sessionManagementService
                .getPastSessionsByPagination(trainerId, pageNo, pageSize,sortDirection);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Deletes a specific session by its session ID.
     * <p>
     * The deletion is performed by verifying the trainer’s ID to prevent unauthorized access.
     * </p>
     *
     * @param sessionId unique identifier of the session to delete
     * @param trainerId unique identifier of the trainer owning the session
     * @return {@link ResponseEntity} with a confirmation message
     * @status 200 OK if deletion is successful
     */
    @DeleteMapping("/trainer/deleteSession")
    public ResponseEntity<GenericResponse> deleteSessionBySessionId(@RequestParam String sessionId,
                                                           @RequestParam String trainerId) {
        log.info("Request received to delete session {} by trainer {}", sessionId, trainerId);
        String response = sessionManagementService.deleteSession(sessionId,trainerId);
        return ResponseEntity.status(HttpStatus.OK).body(new GenericResponse(response));
    }

    @LogExecutionTime
    @PutMapping("/trainer/setStatus")
    public ResponseEntity<GenericResponse> setStatusForSession(
            @RequestParam @NotBlank(message = "Please Provide A Session Id To Update Session Status") String sessionId,
            @RequestParam @NotBlank(message = "Can Not Proceed Without Valid Trainer Id") String trainerId,
            @RequestParam @NotBlank(message = "Please Provide A Valid Status To Update Status") String status
    ) {
        log.info("©️©️ Request received to set staus as {} for session --> {} by trainer --> {} ",
                status,sessionId,trainerId);
        String response = sessionManagementService.setStatusForSession(sessionId,trainerId,status);
        log.info("Sending Response as ::=> {}",response);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new GenericResponse(response));
    }
}
