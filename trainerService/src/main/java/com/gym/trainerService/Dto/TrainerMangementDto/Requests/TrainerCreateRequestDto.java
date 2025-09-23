package com.gym.trainerService.Dto.TrainerMangementDto.Requests;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for creating a new Trainer.
 * <p>
 * Captures all required trainer details for creation, including personal information,
 * contact information, gender, and join date. Includes validation annotations
 * to ensure data correctness before persistence.
 * </p>
 *
 * <p>Validations:</p>
 * <ul>
 *     <li>Id cannot be blank</li>
 *     <li>First and last name are required with a max length of 50 characters</li>
 *     <li>Email must be valid</li>
 *     <li>Phone number must follow a specific regex pattern</li>
 *     <li>Gender must be Male, Female, or Other</li>
 *     <li>Join date cannot be in the future</li>
 * </ul>
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrainerCreateRequestDto {

    @NotBlank(message = "Id cannot be blank")
    private String id;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must be at most 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must be at most 50 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "\\+?\\d{10,15}", message = "Phone number is invalid")
    private String phone;

    @NotBlank(message = "Gender is required")
    @Pattern(regexp = "Male|Female|Other", message = "Gender must be Male, Female or Other")
    private String gender;

    @NotNull(message = "Join date is required")
    @PastOrPresent(message = "Join date cannot be in the future")
    private LocalDate joinDate;

}
