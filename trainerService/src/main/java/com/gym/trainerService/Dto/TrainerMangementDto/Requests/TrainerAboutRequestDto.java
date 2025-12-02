package com.gym.trainerService.Dto.TrainerMangementDto.Requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrainerAboutRequestDto {

    @NotBlank(message = "Can't Edit About of a Trainer Without Having ID")
    private String trainerId;
    @NotBlank(message = "A Trainer's About Can Not Be Empty")
    @NotBlank(message = "A Trainer's About Cannot Be Empty")
    @Size(min = 10, max = 300, message = "About must be between 10 and 300 characters")
    private String about;
}
