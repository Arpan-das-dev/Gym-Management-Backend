package com.gym.authservice.Dto.Request;

import jakarta.validation.constraints.Email;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ForgotPasswordRequestDto {

    @Email(message = "enter a valid email")

    private String  email;

}
