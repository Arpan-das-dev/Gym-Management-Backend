package com.gym.member_service.Dto.MemberFitDtos.Wrappers;

import com.gym.member_service.Dto.MemberFitDtos.Responses.MemberWeighBmiEntryResponseDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberBmiResponseWrapperDto {
    private List<MemberWeighBmiEntryResponseDto> bmiEntryResponseDtoList;
}
