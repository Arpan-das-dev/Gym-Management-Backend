package com.gym.member_service.Dto.MemberFitDtos.Responses;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberBmiResponseWrapperDto {
    List<MemberWeighBmiEntryResponseDto> bmiEntryResponseDtoList;
}
