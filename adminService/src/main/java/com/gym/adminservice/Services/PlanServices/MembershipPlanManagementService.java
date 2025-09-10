package com.gym.adminservice.Services.PlanServices;

import com.gym.adminservice.Dto.PlanDtos.Requests.PlanCreateRequestDto;
import com.gym.adminservice.Dto.PlanDtos.Requests.UpdatePlanRequestDto;
import com.gym.adminservice.Dto.PlanDtos.Responses.CreationResponseDto;
import com.gym.adminservice.Dto.PlanDtos.Responses.UpdateResponseDto;
import com.gym.adminservice.Services.WebClientServices.WebClientPlanService;
import com.gym.adminservice.Utils.PlanIdGenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
@RequiredArgsConstructor

public class MembershipPlanManagementService {

    private final PlanIdGenUtil planIdGenUtil;
    private final WebClientPlanService webClientPlanService;

    @CacheEvict(value = "PlaneCache",allEntries = true)
    public CreationResponseDto createPlan(PlanCreateRequestDto requestDto) {
        String customId = planIdGenUtil.generatePlanId(
                requestDto.getPlanName(), requestDto.getDuration(), requestDto.getPrice()
        );
        CreationResponseDto responseDto = CreationResponseDto.builder()
                .id(customId)
                .planName(requestDto.getPlanName())
                .price(requestDto.getPrice())
                .duration(requestDto.getDuration())
                .features(requestDto.getFeatures())
                .build();

        webClientPlanService.sendCreationToPlanService(responseDto);
        return responseDto;
    }

    @Cacheable(value = "PlaneCache")
    public List<CreationResponseDto> getAllPlans() {
        return webClientPlanService.getAllPlansFromPlanService();
    }

    @CacheEvict(value = "PlaneCache",allEntries = true)
    public UpdateResponseDto updatePlan(String id, UpdatePlanRequestDto requestDto) {
        UpdateResponseDto responseDto = UpdateResponseDto.builder()
                .id(requestDto.getId())
                .planName(requestDto.getPlanName())
                .price(requestDto.getPrice())
                .duration(requestDto.getDuration())
                .features(requestDto.getFeatures())
                .build();

        webClientPlanService.sendUpdateCreationToPlanService(id,requestDto);
        return responseDto;
    }

    @CacheEvict(value = "PlaneCache",allEntries = true)
    public void deletePlan(String id) {
        webClientPlanService.sendDeletionRequestById(id);
    }

}
