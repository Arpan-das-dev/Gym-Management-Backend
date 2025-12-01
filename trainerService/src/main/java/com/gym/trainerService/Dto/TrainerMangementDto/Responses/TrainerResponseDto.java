package com.gym.trainerService.Dto.TrainerMangementDto.Responses;

import com.gym.trainerService.Models.Specialities;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for detailed Trainer response.
 * <p>
 * Encapsulates comprehensive trainer information including personal details,
 * contact info, specialities, last login time, availability, and average rating.
 * Typically used in API responses where full trainer details are required.
 * </p>
 *
 * @author Arpan
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrainerResponseDto {
    private String trainerId;
    private String trainerProfileImageUrl;
    private String firstName;
    private String lastName;
    private String emailId;
    private String phone;
    private String gender;
    private LocalDateTime lastLoginTime;
    private boolean available;
    private double averageRating;
}
