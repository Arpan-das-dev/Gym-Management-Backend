package com.gym.trainerService.Dto.TrainerReviewDto.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponseDto {
    private String reviewId;
    private String userId;
    private String userName;
    private String userRole;
    private LocalDateTime reviewDate;
    private int helpFullVote;
    private int notHelpFullVote;
    private String comment;
    private Double review;
}
