package com.gym.adminservice.Dto.Responses;

import com.gym.adminservice.Enums.RoleType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class TrainerResponseDto {

    private String  firstName;
    private String  lastName;
    private String email;
    private String phone;
    private String password;
    private String gender;
    private LocalDate joinDate;
    private String address;
    private Byte age;
    private List<String> specialties;
    private Byte experience;
}
