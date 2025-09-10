package com.gym.adminservice.Controllers.PlanController;

import com.gym.adminservice.Dto.PlanDtos.Requests.PlanCreateRequestDto;
import com.gym.adminservice.Dto.PlanDtos.Requests.UpdatePlanRequestDto;
import com.gym.adminservice.Dto.PlanDtos.Responses.CreationResponseDto;
import com.gym.adminservice.Dto.PlanDtos.Responses.UpdateResponseDto;
import com.gym.adminservice.Services.PlanServices.MembershipPlanManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${app.plan.url}")
@RequiredArgsConstructor
@Validated
public class MembershipPlanManagementController {

    private final MembershipPlanManagementService planManagementService;

    @PostMapping("createPlan")
    ResponseEntity<CreationResponseDto> createPlan(@Valid @RequestBody PlanCreateRequestDto requestDto){
        CreationResponseDto responseDto = planManagementService.createPlan(requestDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(responseDto);
    }

    @GetMapping("getAll")
    ResponseEntity<List<CreationResponseDto>> getAllPlans(){
        List<CreationResponseDto> responseDtoList = planManagementService.getAllPlans();
        return ResponseEntity.status(HttpStatus.OK).body(responseDtoList);
    }

    @PutMapping("updatePlan")
    ResponseEntity<UpdateResponseDto> updatePlanById(@RequestParam String id,
                                                     @Valid @RequestBody UpdatePlanRequestDto requestDto){
        UpdateResponseDto responseDto = planManagementService.updatePlan(id, requestDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(responseDto);
    }

    @DeleteMapping("delete")
    ResponseEntity<String> deletePlanById(@Valid @RequestParam String id){
        planManagementService.deletePlan(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Request sent to delete");
    }
}
