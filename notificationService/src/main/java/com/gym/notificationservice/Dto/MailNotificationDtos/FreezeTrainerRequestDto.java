package com.gym.notificationservice.Dto.MailNotificationDtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FreezeTrainerRequestDto {
    @NotBlank(message = "Trainer Name Can not be Blank")
    private String trainerName;
    @NotBlank(message = "Please Provide a Valid Trainer's Email")
    @Email(message = "Please Provide Mail in Valid Form")
    private String trainerMail;
    @NotBlank(message = "Unable To Proceed Without having A valid Subject")
    private String subject;
    @NotNull(message = "frozen Status Can not be Null")
    private boolean frozen;
    @NotBlank(message = "Please Provide the time ")
    private String time;
}
