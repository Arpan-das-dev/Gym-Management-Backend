package com.gym.adminservice.Dto.Requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResolveMessageRequestDto {
    @NotBlank(message = "requestId is required")
    private String requestId;

    @Builder.Default
    private boolean notify = false;

    @Size(max = 500, message = "mailMessage cannot be longer than 500 characters")
    private String mailMessage;

    @Builder.Default
    private boolean decline = false;
}
