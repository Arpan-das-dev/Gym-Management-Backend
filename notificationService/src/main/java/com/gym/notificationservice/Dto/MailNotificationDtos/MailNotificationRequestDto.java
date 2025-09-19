package com.gym.notificationservice.Dto.MailNotificationDtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MailNotificationRequestDto {
    @NotBlank(message = "Member ID must not be blank")
    private String memberId;

    @NotBlank(message = "Name must not be blank")
    private String name;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email should be valid")
    private String mailId;

    @NotBlank(message = "Phone must not be blank")
    @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$",
            message = "Phone number is invalid")
    private String phone;

    @NotBlank(message = "Subject must not be blank")
    private String subject;

    @NotNull(message = "Time must be provided")
    private LocalDateTime time;
}
