package com.gym.trainerService.Dto.TrainerReviewDto.Requests;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewRequestDto {

    @NotBlank(message = "User ID cannot be blank")
    private String userId;

    @NotBlank(message = "User name is required")
    @Size(max = 100, message = "User name must be at most 100 characters")
    private String userName;

    @NotBlank(message = "User role is required")
    private String userRole;

    @NotNull(message = "Review date is required")
    @PastOrPresent(message = "Review date cannot be in the future")
    private LocalDateTime reviewDate;

    @Size(max = 500, message = "Comment must be at most 500 characters")
    @NotBlank(message = "Comment is required for review")
    private String comment;

    @NotNull(message = "Review score is required")
    @DecimalMin(value = "0.0", message = "Review must be at least 0.0")
    @DecimalMax(value = "5.0", message = "Review must be at most 5.0")
    private Double review;
}
