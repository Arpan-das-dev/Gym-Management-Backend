package com.gym.authservice.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class LogiInRequestDto {

    @NotBlank(message = "Email or phone number is required")
    private String identifier;

    @NotBlank(message = "Password is required")
    @Size(min = 6,max = 18, message = "password should have minimum 6 character and maximum 18 characters")
    private String password;
}
