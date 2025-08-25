package com.gym.authservice.Entity;

import com.gym.authservice.Roles.RoleType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "users")
public class User {

    @Id
    private String  id;

    private String name;

    @Email
    private String email;

    @Column(unique = true,nullable = false,length = 12)
    private String phoneNo;

    private RoleType role;
    private Integer plansLeftDuration;
}
