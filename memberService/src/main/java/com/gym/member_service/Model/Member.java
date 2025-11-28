package com.gym.member_service.Model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

@Entity
@Table(name = "members",indexes = {
        @Index(name = "idx_plan_expiration",columnList = "plan_expiration"),
        @Index(name = "idx_first_Name", columnList = "firstName"),
        @Index(name = "idx_last_Name", columnList = "lastName"),
        @Index(name = "idx_gender", columnList = "gender"),
        @Index(name = "idx_last_login", columnList = "last_login"),
        @Index(name = "idx_plan_duration_left" , columnList = "plan_duration_left")
})
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
// 1. IMPORTANT: Implement Serializable
public class Member implements Serializable {
    // Generated serialVersionUID (optional but recommended)
    @Serial
    private static final long serialVersionUID = 1L;

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

    // 2. CRITICAL FIX: Add @JsonIgnore to break the loop and prevent LIE.
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY) // Ensure fetch is LAZY
    private Set<WeightBmiEntry> weightBmiEntries ;

    // 3. CRITICAL FIX: Add @JsonIgnore to break the loop and prevent LIE.
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY) // Ensure fetch is LAZY
    private Set<PrProgresses> prProgresses ;

    @Column(name = "login_streak")
    private Integer loginStreak;

    private Integer MaxLoginStreak;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss") // Added Time to match log pattern
    @Column(name = "plan_expiration")
    private LocalDateTime planExpiration;
    private String planID;
    private Boolean activePlan;
    private String planName;
    @Column(name = "plan_duration_left")
    private Integer planDurationLeft;

    private boolean frozen;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;
}