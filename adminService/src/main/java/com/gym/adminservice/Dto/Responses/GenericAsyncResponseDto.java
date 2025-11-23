package com.gym.adminservice.Dto.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Mono;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GenericAsyncResponseDto {
    Mono<String> message;
}
