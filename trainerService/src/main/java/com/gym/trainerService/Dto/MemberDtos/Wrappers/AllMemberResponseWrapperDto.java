package com.gym.trainerService.Dto.MemberDtos.Wrappers;

import com.gym.trainerService.Dto.MemberDtos.Responses.MemberResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AllMemberResponseWrapperDto {
    List<MemberResponseDto> memberResponseDtoList;
}
