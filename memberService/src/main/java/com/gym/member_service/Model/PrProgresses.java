package com.gym.member_service.Model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "pr_progresses")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PrProgresses {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @EqualsAndHashCode.Exclude
    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "workout_name", nullable = false)
    private String workoutName;

    @Column(nullable = false)
    private Double weight;

    @Column(nullable = false)
    private Integer repetitions;

    @Column(name = "achieved_date", nullable = false)
    private LocalDate achievedDate;
}
