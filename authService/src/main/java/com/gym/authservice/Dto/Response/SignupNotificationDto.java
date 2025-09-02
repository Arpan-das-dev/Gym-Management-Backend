package com.gym.authservice.Dto.Response;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SignupNotificationDto {
    private String id;
    private String email;
    private String phone;
    private String name;
}
