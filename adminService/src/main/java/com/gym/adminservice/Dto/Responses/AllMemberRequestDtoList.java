package com.gym.adminservice.Dto.Responses;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class AllMemberRequestDtoList {
   private  List<MemberRequestResponse> requestDtoList;
}
