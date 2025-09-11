package com.gym.member_service.MemberManagementDto.Requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class MemberCreationRequest {
    @NotBlank(message = "first name is required")
    private String  firstName;

    @NotBlank(message = "Last name is required")
    private String  lastName;

    @Email(message = "Enter a valid email account")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Id is required")
    private String id;

    @NotBlank(message = "Phone number is required")
    private String phone;

    @NotBlank(message = "Gender is required")
    private String gender;

    @NotBlank(message = "joining date is required")
    private LocalDate joinDate;
}
