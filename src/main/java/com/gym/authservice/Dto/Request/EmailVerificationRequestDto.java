package com.gym.authservice.Dto.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmailVerificationRequestDto {

    @Email(message = "this email is not valid")
    private String email;

    @NotBlank
    @Size(min = 6,max = 6,message = "otp should be in six digit")
    private String otp;

}
