package com.gym.member_service.Controllers;

import com.gym.member_service.Dto.MemberPlanDto.Requests.MembersPlanMeticsRequestDto;
import com.gym.member_service.Dto.MemberPlanDto.Requests.PlanRequestDto;
import com.gym.member_service.Dto.MemberPlanDto.Responses.MemberPlanInfoResponseDto;
import com.gym.member_service.Dto.MemberPlanDto.Responses.MemberPlansMeticsResponseDto;
import com.gym.member_service.Services.FeatureServices.MemberPlanSerVice;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * Controller responsible for managing member subscription plans.
 *
 * <p>Interacts with MemberPlanService to handle plan updates, retrieving plan information,
 * and fetching plan analytics.
 *
 * <p>The base URL is externally configurable. Administrative endpoints are clearly
 * designated.
 *
 * Responsibilities include:
 * <ul>
 *   <li>Updating member plans with additive duration handling</li>
 *   <li>Fetching detailed plan information for a specific member</li>
 *   <li>Retrieving aggregated plan metrics for administrative analysis</li>
 * </ul>
 *
 * Request and response payloads are validated and logged for visibility.
 * Appropriate HTTP statuses are used to conform to REST principles.
 *
 * @author Arpan Das
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("${member-service.BASE_URL}")
@RequiredArgsConstructor

public class MemberPlanController {

    private final MemberPlanSerVice planSerVice;

    /**
     * Updates the plan subscription for a specified member.
     *
     * <p>Accepts member ID and plan details to process additive plan duration extensions.
     * Logs update requests and returns status with HTTP 202 ACCEPTED.
     *
     * @param id member ID whose plan is updated
     * @param requestDto DTO containing plan update details including plan ID, name, and duration
     * @return ResponseEntity with confirmation message
     */
    @PostMapping("plan")
    public ResponseEntity<String> updatePlanInfo(@RequestParam String id,
                                                 @RequestBody PlanRequestDto requestDto) {
        log.info("Received plan update request for member id: {}", id);
        String response = planSerVice.updatePlan(id, requestDto);
        log.info("Plan updated successfully for member id: {}", id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * Retrieves detailed plan information for a specific member.
     *
     * <p>Validates member ID and logs retrieval operation.
     * Returns plan details including remaining duration with HTTP 200 OK.
     *
     * @param memberId non-blank member ID parameter
     * @return ResponseEntity containing member's plan information
     */
    @GetMapping("planDetails")
    public ResponseEntity<MemberPlanInfoResponseDto> getPlanInfoByMemberId(
            @RequestParam @NotBlank(message = "Failed to retrieve data: member ID cannot be empty") String memberId) {
        log.info("Request received for plan info of member id: {}", memberId);
        MemberPlanInfoResponseDto response = planSerVice.getMemberPlanDetails(memberId);
        log.info("Returning plan info for member id: {} with days left: {}", memberId, response.getPlanDurationLeft());
        return ResponseEntity.ok(response);
    }

    /**
     * Fetches aggregated member plan metrics for administrative reporting.
     *
     * <p>Accepts a list of plan names and returns metrics including member counts and detailed data.
     * Validates request payload and logs request for transparency.
     *
     * @param requestDto DTO containing list of plan names for which metrics are requested
     * @return ResponseEntity containing list of plan metrics response DTOs
     */
    @GetMapping("admin/matrics")
    public ResponseEntity<List<MemberPlansMeticsResponseDto>> getAllMetrics(
            @RequestBody @Valid MembersPlanMeticsRequestDto requestDto) {
        log.info("Received request for plan metrics: {}", requestDto.getPlanNames());
        List<MemberPlansMeticsResponseDto> response = planSerVice.getAllMatrices(requestDto);
        log.info("Returning metrics for {} plans", response.size());
        return ResponseEntity.ok(response);
    }
}
