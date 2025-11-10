package com.gym.planService.Dtos.PlanDtos.Wrappers;

import com.gym.planService.Dtos.PlanDtos.Responses.PlanResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Wrapper Data Transfer Object (DTO) encapsulating a list of plan response DTOs.
 * <p>
 * This class is used to standardize the API response format when returning multiple subscription plans.
 * It packages a collection of {@link PlanResponseDto} objects within a single container.
 * </p>
 *
 * <p>Example JSON representation:</p>
 * <pre>
 * {
 *   "responseDtoList": [
 *     {
 *       "planId": "BASIC_2026",
 *       "planName": "Basic Plan",
 *       "price": 2999.0,
 *       "duration": 365,
 *       "planFeatures": ["Feature 1", "Feature 2"]
 *     },
 *     {
 *       "planId": "PREMIUM_2026",
 *       "planName": "Premium Plan",
 *       "price": 5999.0,
 *       "duration": 365,
 *       "planFeatures": ["Feature A", "Feature B", "Feature C"]
 *     }
 *   ]
 * }
 * </pre>
 *
 * <p>This wrapper facilitates handling lists of plans smoothly in REST API communications.</p>
 *
 * @author : Arpan Das
 * @version : 1.0
 * @since : 2025-10-19
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AllPlanResponseWrapperResponseDto {

    /**
     * List of plan response DTOs.
     * This list contains individual plan details wrapped in {@link PlanResponseDto}.
     */
    private List<PlanResponseDto> responseDtoList;
}
