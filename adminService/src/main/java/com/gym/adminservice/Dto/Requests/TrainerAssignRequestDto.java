package com.gym.adminservice.Dto.Requests;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;

@AllArgsConstructor
@Getter
@Builder
@NoArgsConstructor
public class TrainerAssignRequestDto {

    @NotBlank(message = "MemberId can not be blank")
    private String memberId;

    @NotBlank(message = "Member's profile image can not be blank")
    private String memberProfileImageUrl;

    @NotBlank(message = "memberName can not be empty")
    private String memberName;

    @NotBlank(message = "TrainerId can not be blank")
    private String trainerId;

    @NotBlank(message = "Trainer's profile image can not be blank")
    private String trainerProfileImageUrl;

    @NotBlank(message = "TrainerName can not be empty")
    private String trainerName;

    @PastOrPresent(message = "Request date cannot be in the future")
    private LocalDate requestDate;

    @NotBlank(message = "Plan name can not be empty")
    private String memberPlanName;

    private LocalDate memberPlanExpirationDate;
}
