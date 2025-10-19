package com.gym.planService.Dtos.CuponDtos.Wrappers;

import com.gym.planService.Dtos.CuponDtos.Responses.CuponCodeResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Wrapper Data Transfer Object (DTO) that encapsulates a list of coupon code responses.
 * <p>
 * This class is primarily used to return multiple {@link CuponCodeResponseDto} objects,
 * providing a structured format for API responses that involve multiple coupons.
 * </p>
 *
 * <p>Example JSON representation:</p>
 * <pre>
 * {
 *   "responseDtoList": [
 *     {
 *       "cuponCode": "SPRING50",
 *       "validityDate": "2026-04-30",
 *       "offPercentage": 50.0
 *     },
 *     {
 *       "cuponCode": "WELCOME10",
 *       "validityDate": "2026-12-31",
 *       "offPercentage": 10.0
 *     }
 *   ]
 * }
 * </pre>
 *
 * <p>This wrapper facilitates handling collections of coupon codes in a standardized response object.</p>
 *
 * @author Arpan Das
 * @version 1.0
 * @since 2025-10-19
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AllCuponCodeWrapperResponseDto {

    /**
     * List of coupon code response DTOs.
     * Contains multiple coupon details wrapped in {@link CuponCodeResponseDto}.
     */
    private List<CuponCodeResponseDto> responseDtoList;
}
