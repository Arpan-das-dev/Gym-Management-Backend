package com.gym.adminservice.Controllers.PlanController;

import com.gym.adminservice.Dto.PlanDtos.Requests.PlanCreateRequestDto;
import com.gym.adminservice.Dto.PlanDtos.Requests.UpdatePlanRequestDto;
import com.gym.adminservice.Dto.PlanDtos.Responses.CreationResponseDto;
import com.gym.adminservice.Dto.PlanDtos.Responses.UpdateResponseDto;
import com.gym.adminservice.Dto.Responses.GenericResponseDto;
import com.gym.adminservice.Exceptions.Custom.PlanNotFounException;
import com.gym.adminservice.Services.PlanServices.MembershipPlanManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequestMapping("${app.plan.url}")
@RequiredArgsConstructor
@Validated
public class MembershipPlanManagementController {

    private final MembershipPlanManagementService planManagementService;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    @PostMapping("createPlan")
    ResponseEntity<GenericResponseDto> createPlan(@Valid @RequestBody PlanCreateRequestDto requestDto){
        log.info("{} Received request to create plan with name: {}",
                LocalDateTime.now().format(formatter), requestDto.getPlanName());
        try {
            log.info("{}:: Successfully created the plan:: {}",
                    LocalDateTime.now().format(formatter),requestDto.getPlanName());
            GenericResponseDto responseDto = new GenericResponseDto(planManagementService.createPlan(requestDto));
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(responseDto);

        } catch (ExecutionException | InterruptedException e) {
            log.warn("Failed to create plan due to {} ",e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    @GetMapping("getAll")
    ResponseEntity<List<CreationResponseDto>> getAllPlans(){

        List<CreationResponseDto> responseDtoList = planManagementService.getAllPlans();
        return ResponseEntity.status(HttpStatus.OK).body(responseDtoList);
    }

    @PutMapping("updatePlan")
    ResponseEntity<GenericResponseDto> updatePlanById(@RequestParam String id,
                                                      @Valid @RequestBody UpdatePlanRequestDto requestDto){
        log.info("Received request to update plan with id: {}", id);
        try {
            log.info("{}:: Successfully updated the plan:: {}",
                    LocalDateTime.now().format(formatter),requestDto.getPlanName());
            GenericResponseDto responseDto = new GenericResponseDto(planManagementService.updatePlan(id, requestDto));
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(responseDto);
        } catch (ExecutionException | InterruptedException e) {
            log.warn("Failed to update plan due to {} ",e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    @DeleteMapping("delete")
    ResponseEntity<GenericResponseDto> deletePlanById(@Valid @RequestParam String id){
        log.info("Received request to delete plan with id: {}", id);
        try {
            log.info("{}:: Successfully deleted  the plan:: {}",
                    LocalDateTime.now().format(formatter),id);
            GenericResponseDto response = new GenericResponseDto(planManagementService.deletePlan(id));
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        } catch (Exception e ) {
            log.warn("Failed to delete plan due to {} ",e.getLocalizedMessage());
            if(e.getCause() instanceof PlanNotFounException ex) throw  ex;
            throw new RuntimeException(e);
        }
    }
}
