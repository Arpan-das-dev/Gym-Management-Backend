package com.gym.trainerService.Dto.TrainerMangementDto.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FreezeTrainerResponseDto {
    private String trainerName;
    private String trainerMail;
    private String subject;
    private boolean frozen;
    private String time;
}
