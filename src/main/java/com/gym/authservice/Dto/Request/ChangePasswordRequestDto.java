package com.gym.authservice.Dto.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ChangePasswordRequestDto {

    @Email(message = "enter a valid email")
    private String email;

    @NotBlank(message = "password is required")
    @Size(min = 6,max = 18, message = "password should have minimum 6 character and maximum 18 characters")
    private String password;
}
