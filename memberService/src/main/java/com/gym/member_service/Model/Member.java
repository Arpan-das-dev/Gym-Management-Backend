package com.gym.member_service.Model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

@Entity
@Table(name = "members")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Member {
    @Id
    private String id;

    private String profileImageUrl;

    @Column(name = "firstName", nullable = false)
    private String firstName;

    @Column(name = "lastName", nullable = false)
    private String lastName;

    @Email
    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "gender", nullable = false)
    private String gender;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "joinDate", nullable = false)
    private LocalDate joinDate;

    @Column(name ="Current_bmi")
    private double currentBmi;

    @Transient
    private boolean activeInGym;

    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<WeightBmiEntry> weightBmiEntries ;

    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PrProgresses> prProgresses ;

    @Column(name = "login_streak")
    private Integer loginStreak;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "plan_expiration")
    private LocalDateTime planExpiration;
    private String planID;
    private Boolean activePlan;
    private String planName;
    private Integer planDurationLeft;

    private boolean frozen;

    private LocalDateTime lastLogin;
}
