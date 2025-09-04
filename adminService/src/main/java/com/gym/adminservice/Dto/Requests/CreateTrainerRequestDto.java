package com.gym.adminservice.Dto.Requests;

import com.gym.adminservice.Enums.RoleType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class CreateTrainerRequestDto {

    @NotBlank(message = "firstName is required")
    private String  firstName;

    @NotBlank(message = "LastName is required")
    private String  lastName;

    @Email(message = "Enter a valid email id")
    @NotBlank(message = "email can not be empty")
    private String email;

    @NotBlank (message = "phone no is required")
    @Pattern(regexp = "^[0-9]+$", message = "Phone number must contain only digits")
    @Size(min = 10,max = 12,message = "Phone no can be between 10 to 12 digits")
    private String phone;

    @NotBlank(message = "password is required")
    @Size(min = 6, message = "minimum 6 characters is needed")
    private String password;

    @NotBlank(message = "Gender is required")
    private String gender;

    @NotNull(message = "Please Select a role to signUp")
    private RoleType role;

    @NotNull(message = "joining date can not be empty")
    private LocalDate joinDate;

    @NotBlank(message = "address is required")
    private String address;

    @NotNull(message = "age is required")
    @NotBlank(message = "age is required")
    private Byte age;

    @NotBlank(message = " at least one specialties required")
    private List<String> specialties;

    @Min(value = 0,message = "experience can not be negative")
    private Byte experience;
}
