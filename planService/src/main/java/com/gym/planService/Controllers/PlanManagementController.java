package com.gym.planService.Controllers;

import com.gym.planService.Dtos.PlanDtos.Requests.PlanCreateRequestDto;
import com.gym.planService.Dtos.PlanDtos.Requests.PlanUpdateRequestDto;
import com.gym.planService.Dtos.PlanDtos.Responses.PlanResponseDto;
import com.gym.planService.Dtos.PlanDtos.Wrappers.AllPlanResponseWrapperDto;
import com.gym.planService.Services.PlanManagementService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("${plan-service.Base_Url.planManagement}")
public class PlanManagementController {

    private final PlanManagementService planManagementService;

    @PostMapping("/admin/addPlan")
    public ResponseEntity<AllPlanResponseWrapperDto> createPlanBYAdmin(
            @Valid @RequestBody PlanCreateRequestDto requestDto) {
        log.info("Request received to create plan of name::{} with id::{}",
                requestDto.getPlanName(),requestDto.getPlanId());
        AllPlanResponseWrapperDto response = planManagementService.createPlan(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/admin/updatePlan")
    public ResponseEntity<PlanResponseDto> updatePlanByAdmin(
            @NotBlank @RequestParam String id,
            @Valid @RequestBody PlanUpdateRequestDto requestDto) {
        log.info("Request received to update plan of name::{} with id::{}",
                requestDto.getPlanName(),id);
        PlanResponseDto response = planManagementService.updatePlan(id,requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/admin/deletePlan")
    public ResponseEntity<String> deletePlanById(@Valid @NotBlank @RequestParam String id) {
        log.info("Request received to delete plan of id::{}", id);
        String response = planManagementService.deletePlan(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/all/getPlans")
    public ResponseEntity<AllPlanResponseWrapperDto> getAllPlans() {
        log.info("Request received to get all plans on {}", LocalDate.now());
        AllPlanResponseWrapperDto response = planManagementService.getAllPlans();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
