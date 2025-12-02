package com.gym.trainerService.Dto.TrainerMangementDto.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PublicTrainerInfoResponseDto {
    private String id;
    private String firstName;
    private String lastName;
    private String about;
    private int clientCount;
    private String email;
    private String gender;
    private double averageRating;
    private int reviewCount;
    List<String> specialities;
}
