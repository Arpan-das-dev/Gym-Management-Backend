package com.gym.adminservice.Services.PlanServices;

import com.gym.adminservice.Dto.PlanDtos.Requests.PlanCreateRequestDto;
import com.gym.adminservice.Dto.PlanDtos.Requests.UpdatePlanRequestDto;
import com.gym.adminservice.Dto.PlanDtos.Responses.CreationResponseDto;
import com.gym.adminservice.Exceptions.Custom.PlanNotFounException;
import com.gym.adminservice.Services.WebClientServices.WebClientPlanService;
import com.gym.adminservice.Utils.PlanIdGenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor

public class MembershipPlanManagementService {

    private final PlanIdGenUtil planIdGenUtil;
    private final WebClientPlanService webClientPlanService;


    public String createPlan(PlanCreateRequestDto requestDto) throws ExecutionException, InterruptedException {
        String customId = planIdGenUtil.generatePlanId(
                requestDto.getPlanName(), requestDto.getDuration(), requestDto.getPrice()
        );
        CreationResponseDto responseDto = CreationResponseDto.builder()
                .planId(customId)
                .planName(requestDto.getPlanName())
                .price(requestDto.getPrice())
                .duration(requestDto.getDuration())
                .features(requestDto.getFeatures())
                .build();

        String response =  webClientPlanService.sendCreationToPlanService(responseDto).get();
        log.info("response received from plan service to create plan ::{}",response);
        return response;
    }


    public List<CreationResponseDto> getAllPlans() {
        return webClientPlanService.getAllPlansFromPlanService();
    }


    public String  updatePlan(String id, UpdatePlanRequestDto requestDto) throws ExecutionException, InterruptedException {
        String response =  webClientPlanService.sendUpdateCreationToPlanService(id,requestDto).get();
        log.info("response received from plan service to update plan ::{}",response);
        return response;
    }


    public String  deletePlan(String id) throws ExecutionException, InterruptedException, PlanNotFounException {
        String response =  webClientPlanService.sendDeletionRequestById(id).get();
        log.info("response received from plan service to delete plan ::{}",response);
        return response;
    }

}
