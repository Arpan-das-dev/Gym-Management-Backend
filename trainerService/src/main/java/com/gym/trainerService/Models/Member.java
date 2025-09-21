package com.gym.trainerService.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "trainers", indexes = {
        @Index(name = "idx_member_id", columnList = "memberId"),
        @Index(name = "idx_trainer_id", columnList = "trainerId")
})
public class Member {
    @Id
    private String memberId;

    @Column(nullable = false)
    private String memberName;

    @Column(name = "trainer_url")
    private String memberProfileImageUrl;

    @Column(nullable = false)
    private String trainerId;

    @Column(nullable = false)
    private LocalDate eligibilityEnd;
}
