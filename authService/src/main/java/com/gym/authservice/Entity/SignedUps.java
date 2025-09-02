package com.gym.authservice.Entity;

import com.gym.authservice.Roles.RoleType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "signedUp")
public class SignedUps {
    @Id
    @Column(name = "gymId")
    private String id;

    @Column(name = "firstName", nullable = false)
    private String firstName;

    @Column(name = "lastName", nullable = false)
    private String lastName;

    @Column(name = "gender", nullable = false)
    private String gender;

    @Email
    @Column(name = "UserMail", nullable = false, unique = true)
    private String email;

    @Column(name = "phoneNo", length = 12, nullable = false, unique = true)
    private String phone;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private RoleType role;

    @Column(name = "joined_on", nullable = false)
    private LocalDate joinDate;

    @Column(name = "isVerifiedEmail", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "isVerifiedPhone", nullable = false)
    private boolean phoneVerified = false;

    @Column(name = "approved", nullable = false)
    private boolean isApproved = false;
}