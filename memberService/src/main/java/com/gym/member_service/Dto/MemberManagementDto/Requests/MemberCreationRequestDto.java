package com.gym.member_service.Dto.MemberManagementDto.Requests;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class MemberCreationRequestDto {
    @NotBlank(message = "Id must not be blank")
    private String id;

    @NotBlank(message = "First name must not be blank")
    private String firstName;

    @NotBlank(message = "Last name must not be blank")
    private String lastName;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email must not be blank")
    private String email;

    @Pattern(regexp = "\\+?[0-9]{7,15}", message = "Phone number must be valid")
    private String phone;

    @NotNull(message = "Gender must not be null")
    private String gender;

    @NotNull(message = "Join date must not be null")
    @PastOrPresent(message = "Join date cannot be in the future")
    private LocalDate joinDate;

}
