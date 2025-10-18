package com.gym.planService.Dtos.CuponDtos.Wrappers;

import com.gym.planService.Dtos.CuponDtos.Responses.CuponCodeResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AllCuponCodeWrapperResponseDto {
    List<CuponCodeResponseDto>responseDtoList;
}
