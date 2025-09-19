package com.gym.notificationservice.Dto.MemberNotificationRequests;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlanActivationNotificationRequestDto {
    @NotBlank(message = "Plan name must not be blank")
    private String planName;

    @NotBlank(message = "Subject must not be blank")
    private String subject;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email should be valid")
    private String mailId;

    @NotBlank(message = "Phone must not be blank")
    @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$",
            message = "Phone number is invalid")
    private String phone;

    @NotNull(message = "Activation date must be provided")
    private LocalDate activationDate;

    @NotNull(message = "Plan expiration date must be provided")
    private LocalDate planExpiration;

    @NotNull(message = "Duration must be provided")
    @Positive(message = "Duration must be a positive number")
    private Integer duration;
}
