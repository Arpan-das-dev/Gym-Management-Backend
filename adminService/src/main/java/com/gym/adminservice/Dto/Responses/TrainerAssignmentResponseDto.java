package com.gym.adminservice.Dto.Responses;

import java.time.LocalDate;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TrainerAssignmentResponseDto {
    private String trainerId;
    private String trainerName;
    private String trainerProfileImageUrl;
    private LocalDate eligibilityEnd;
    private String memberId;
}
