package com.gym.authservice.Entity;

import com.gym.authservice.Roles.RoleType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;
import jakarta.validation.constraints.Email;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table("signed_up")
public class SignedUps {

    @Id
    @Column("gym_id")
    private String id;

    @Column("first_name")
    private String firstName;

    @Column("last_name")
    private String lastName;

    @Column("gender")
    private String gender;

    @Email
    @Column("user_mail")
    private String email;

    @Column("phone_no")
    private String phone;

    @Column("password")
    private String password;

    @Column("role")
    private RoleType role;

    @Column("joined_on")
    private LocalDate joinDate;

    @Column("is_verified_email")
    private boolean emailVerified;

    @Column("is_verified_phone")
    private boolean phoneVerified;

    @Column("approved")
    private boolean approved;
}
