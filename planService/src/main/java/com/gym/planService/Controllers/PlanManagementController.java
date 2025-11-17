package com.gym.planService.Controllers;

import com.gym.planService.Dtos.PlanDtos.Requests.PlanCreateRequestDto;
import com.gym.planService.Dtos.PlanDtos.Requests.PlanUpdateRequestDto;
import com.gym.planService.Dtos.PlanDtos.Responses.PlanResponseDto;
import com.gym.planService.Dtos.PlanDtos.Wrappers.AllPlanResponseWrapperResponseDto;
import com.gym.planService.Services.PlanServices.PlanManagementService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * REST controller responsible for managing subscription plans.
 * <p>
 * Provides administrative APIs to create, update, delete, and retrieve plans.
 * Each method is annotated for validation and logs key request details.
 * </p>
 *
 * <p>Typical usage involves admin users triggering plan lifecycle actions.</p>
 *
 * <p>Example request to create a plan:</p>
 * <pre>
 * {
 *   "planId": "BASIC_2026",
 *   "planName": "Basic Plan",
 *   "description": "A base subscription plan with limited features."
 * }
 * </pre>
 *
 * @author : Arpan Das
 * @version : 1.0
 * @since : 2025-10-19
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("${plan-service.Base_Url.planManagement}")
public class PlanManagementController {

    private final PlanManagementService planManagementService;

    /**
     * Creates a new subscription plan.
     *
     * @param requestDto the request body containing plan creation details
     * @return a wrapper DTO containing all plans including the newly created one, with status 201 Created
     */
    @PostMapping("/admin/addPlan")
    public ResponseEntity<String> createPlanBYAdmin(
            @Valid @RequestBody PlanCreateRequestDto requestDto) {
        log.info("Request received to create plan of name::{} with id::{}",
                requestDto.getPlanName(), requestDto.getPlanId());
        AllPlanResponseWrapperResponseDto responseDto =  planManagementService.createPlan(requestDto);
        log.info("Successfully created a new plan and all {} loaded to cache again",
                responseDto.getResponseDtoList().size());
        return ResponseEntity.status(HttpStatus.CREATED).body("New Plan Created Successfully");
    }

    /**
     * Updates an existing subscription plan identified by id.
     *
     * @param id         the plan ID to update; must be non-blank
     * @param requestDto the update details for the plan; validated
     * @return the updated plan DTO with status 201 Created
     */
    @PutMapping("/admin/updatePlan")
    public ResponseEntity<String> updatePlanByAdmin(
            @NotBlank(message = "Plan ID must be provided") @RequestParam String id,
            @Valid @RequestBody PlanUpdateRequestDto requestDto) {
        log.info("Request received to update plan of name::{} with id::{}",
                requestDto.getPlanName(), id);
        PlanResponseDto responseDto =  planManagementService.updatePlan(id, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Successfully updated "+responseDto.getPlanName());
    }

    /**
     * Deletes a subscription plan by its ID.
     *
     * @param id the plan ID to delete; must be non-blank
     * @return a confirmation message with status 202 Accepted
     */
    @DeleteMapping("/admin/deletePlan")
    public ResponseEntity<String> deletePlanById(
            @Valid @NotBlank(message = "Plan ID must be provided") @RequestParam String id) {
        log.info("Request received to delete plan of id::{}", id);
        AllPlanResponseWrapperResponseDto response = planManagementService.deletePlan(id);
        log.info("Plan deleted successfully and cache is updated of new {} no of plans",
                response.getResponseDtoList().size());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Successfully deleted plan ");

    }

    /**
     * Retrieves all available subscription plans.
     *
     * @return a wrapper DTO containing all plans with status 200 OK
     */
    @GetMapping("/all/getPlans")
    public ResponseEntity<AllPlanResponseWrapperResponseDto> getAllPlans() {
        log.info("Request received to get all plans on {}", LocalDate.now());
        AllPlanResponseWrapperResponseDto response = planManagementService.getAllPlans();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("all/memberCount")
    public ResponseEntity<String> decreaseMembersCount(@Valid @RequestParam String planId){
        LocalDate date = LocalDate.now();
        log.info("{}::{}::{}Request received to decrement members count for plan:: {}"
               ,date.getYear(),date.getMonthValue(),date.getDayOfMonth() ,planId);
        String response = planManagementService.decrementMemberCount(planId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
