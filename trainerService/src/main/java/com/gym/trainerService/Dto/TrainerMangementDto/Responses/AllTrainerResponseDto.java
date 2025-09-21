package com.gym.trainerService.Dto.TrainerMangementDto.Responses;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AllTrainerResponseDto {
    private String id;
    private String imageUrl;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String gender;
    private double averageRating;
    private LocalDateTime lastLoginTime;
}
