package com.gym.member_service.Dto.MemberTrainerDtos.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Mono;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GenericAsyncResponseDto {
    private Mono<String> message;
}
