package com.gym.trainerService.Dto.SessionDtos.Wrappers;

import com.gym.trainerService.Dto.SessionDtos.Responses.AllSessionResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AllSessionsWrapperDto {
    List<AllSessionResponseDto> responseDtoList;
}
