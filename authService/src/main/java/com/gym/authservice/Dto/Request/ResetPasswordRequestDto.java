package com.gym.authservice.Dto.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordRequestDto {
    @Email(message = "please enter a valid email")
    private String email;
    private String oldPassword;

    @NotBlank(message = "new password can not be empty")
    @Size(min = 6,max = 18, message = "password should have minimum 6 character and maximum 18 characters")
    private String newPassword;
}
