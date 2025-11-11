package com.gym.adminservice.Dto.Wrappers;

import com.gym.adminservice.Dto.Responses.PendingRequestResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AllPendingRequestResponseWrapperDto {
    List<PendingRequestResponseDto> responseDtoList;
}
