package com.gym.member_service.Controllers;

import com.gym.member_service.Dto.MemberManagementDto.Requests.FreezeRequestDto;
import com.gym.member_service.Dto.MemberManagementDto.Requests.MemberCreationRequestDto;
import com.gym.member_service.Dto.MemberManagementDto.Responses.LoginStreakResponseDto;
import com.gym.member_service.Dto.MemberManagementDto.Responses.MemberInfoResponseDto;
import com.gym.member_service.Dto.MemberManagementDto.Wrappers.AllMembersInfoWrapperResponseDtoList;
import com.gym.member_service.Dto.NotificationDto.GenericResponse;
import com.gym.member_service.Services.MemberServices.MemberManagementService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing member accounts including creation, details retrieval,
 * account status changes, streak tracking, and deletion.
 *
 * <p>The base URL is configured externally, and endpoints prefixed with 'admin'
 * are restricted to administrative users.
 *
 * <p>Endpoints reactively interact with MemberManagementService for business logic.
 * Logging captures key request parameters and transactional context for monitoring.
 * Responsibilities include:
 * <ul>
 *   <li>Creating new members</li>
 *   <li>Freezing or unfreezing member accounts</li>
 *   <li>Setting and getting login streaks</li>
 *   <li>Fetching member basic details</li>
 *   <li>Admin-level listing of members with pagination and filtering</li>
 *   <li>Deleting member accounts</li>
 * </ul>
 *
 * @author Arpan Das
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("${member-service.BASE_URL}")
@RequiredArgsConstructor
@Validated

public class MemberManagementController {
    private final MemberManagementService memberManagementService;

    /**
     * Creates a new member account.
     *
     * <p>This endpoint is available for authorized services to request member creation.
     * Validates request payload and returns confirmation with HTTP 201 CREATED status.
     *
     * @param requestDto validated member creation details
     * @return ResponseEntity with GenericResponse confirming creation
     */
    @PostMapping("create")
    public ResponseEntity<GenericResponse> createMember(@Valid @RequestBody MemberCreationRequestDto requestDto) {
        log.info("Received request to create member with email: {}", requestDto.getEmail());
        String response = memberManagementService.createMember(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new GenericResponse(response));
    }

    /**
     * Freezes or unfreezes a member account.
     *
     * <p>Accessible by admins to manage member account states.
     * Returns HTTP 202 ACCEPTED with operation confirmation.
     *
     * @param requestDto freeze/unfreeze request details
     * @return ResponseEntity with GenericResponse indicating status
     */
    @PostMapping("admin/freeze")
    public ResponseEntity<GenericResponse> freeze(@Valid @RequestBody FreezeRequestDto requestDto) {
        log.info("Admin requested account freeze/unfreeze for member id: {}", requestDto.getId());
        String response = memberManagementService.freezeOrUnFrozen(requestDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new GenericResponse(response));
    }

    /**
     * Sets or increments the login streak count for a member.
     *
     * <p>Validates the member ID and returns current streak value.
     *
     * @param id non-blank member ID
     * @return ResponseEntity containing current login streak information
     */
    @PostMapping("setStreak")
    public ResponseEntity<LoginStreakResponseDto> setLoginStreak(
            @RequestParam @NotBlank(message = "Cannot proceed with empty member ID") String id) {
        log.info("Setting login streak for member id: {}", id);
        LoginStreakResponseDto response = memberManagementService.setLoginStreak(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the current login streak for a member.
     *
     * <p>Validates input and returns streak details with HTTP 200 OK.
     *
     * @param id non-blank member ID
     * @return ResponseEntity with current login streak info
     */
    @GetMapping("getStreak")
    public ResponseEntity<LoginStreakResponseDto> getLoginStreak(
            @RequestParam @NotBlank(message = "Cannot proceed with empty member ID") String id) {
        log.info("Retrieving login streak for member id: {}", id);
        LoginStreakResponseDto response = memberManagementService.getLoginStreak(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves basic details of a specific member by ID.
     *
     * <p>Response includes limited profile information for frontend usage.
     *
     * @param id member ID
     * @return ResponseEntity containing member info DTO
     */
    @GetMapping("getBy")
    public ResponseEntity<MemberInfoResponseDto> getMemberById(@RequestParam String id) {
        log.info("Request received to get member details for id: {}", id);
        MemberInfoResponseDto response = memberManagementService.getMemberById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a paginated and filterable list of all members for administrative dashboard.
     *
     * <p>Supports filtering by search text, gender, and status.
     * Sorting is configurable with direction and field.
     * Pagination parameters control page size and number.
     * Logs all query parameters for audit and debugging.
     *
     * @param searchBy optional filter string
     * @param gender optional gender filter
     * @param status optional membership status filter
     * @param sortBy field to sort by, defaults to 'planExpiration'
     * @param sortDirection sort direction, either 'asc' or 'desc'
     * @param pageNo zero-based page number, defaults to 0
     * @param pageSize number of records per page, defaults to 20
     * @return ResponseEntity wrapping list DTO of member info
     */
    @GetMapping("admin/getAll")
    public ResponseEntity<AllMembersInfoWrapperResponseDtoList> getAllMembers(
            @RequestParam(required = false, defaultValue = "") String searchBy,
            @RequestParam(required = false, defaultValue = "") String gender,
            @RequestParam(required = false, defaultValue = "") String status,
            @RequestParam(required = false, defaultValue = "planExpiration") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection,
            @RequestParam(required = false, defaultValue = "0") Integer pageNo,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize
    ) {
        pageNo = Math.max(pageNo, 0);
        pageSize = pageSize < 0 ? 20 : pageSize;

        log.info("Admin Transaction Request | page={} | size={} | searchBy='{}' | sortBy='{}' | direction='{}' | gender='{}' | status='{}'",
                pageNo, pageSize, searchBy, sortBy, sortDirection, gender, status);

        AllMembersInfoWrapperResponseDtoList response = memberManagementService
                .getAllMember(searchBy, gender, status, sortBy, sortDirection, pageNo, pageSize);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a member by ID.
     *
     * <p>Accessible by authorized admin services to remove user accounts.
     * Returns HTTP 202 ACCEPTED with operation result.
     *
     * @param id the member ID to delete
     * @return ResponseEntity with a GenericResponse message
     */
    @DeleteMapping("admin/delete")
    public ResponseEntity<GenericResponse> deleteMemberById(@RequestParam String id) {
        log.info("Admin requested deletion of member with id: {}", id);
        String response = memberManagementService.deleteMemberById(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new GenericResponse(response));
    }
}