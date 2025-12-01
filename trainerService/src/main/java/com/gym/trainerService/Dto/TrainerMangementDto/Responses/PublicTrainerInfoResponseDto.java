package com.gym.trainerService.Dto.TrainerMangementDto.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;

@Deprecated
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BasicTrainerInfoResponseDto {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String gender;
    private List<String> specialities;
    private double averageRating;
}
