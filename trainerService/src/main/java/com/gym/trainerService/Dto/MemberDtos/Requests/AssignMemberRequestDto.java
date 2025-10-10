package com.gym.trainerService.Dto.MemberDtos.Requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssignMemberRequestDto {
    @NotBlank(message = "Member ID must not be blank")
    private String memberId;

    @NotBlank(message = "Trainer ID must not be blank")
    private String trainerId;

    @NotBlank(message = "MemberName can not be empty")
    private String memberName;

    @NotBlank(message = "Trainer profile image URL must be a valid URL")
    private String trainerProfileImageUrl;

    @NotNull(message = "Request date must not be null")
    @PastOrPresent(message = "Request date cannot be in the future")
    private LocalDate requestDate;
}
