package com.gym.trainerService.Dto.TrainerMangementDto.Responses;


import com.gym.trainerService.Dto.TrainerMangementDto.Wrappers.AllTrainerResponseDtoWrapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for a summarized Trainer response.
 * <p>
 * Provides basic trainer information suitable for listing trainers,
 * including name, contact info, profile image URL, average rating, and last login time.
 * Typically used in wrapper responses for all trainers.
 * </p>
 *
 * @see AllTrainerResponseDtoWrapper
 * @author Arpan
 * @version 1.0
 * @since 1.0
 */
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
