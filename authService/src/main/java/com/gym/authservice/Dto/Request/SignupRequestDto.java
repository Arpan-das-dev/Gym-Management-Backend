package com.gym.authservice.Dto.Request;

import com.gym.authservice.Roles.RoleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequestDto {
    @NotBlank(message = "firstName is required")
    private String  firstName;
    @NotBlank(message = "LastName is required")
    private String  lastName;
    @Email (message = "Enter a valid email id")
    @NotBlank(message = "email can not be empty")
    private String email;
   @NotBlank (message = "phone no is required")
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

}
