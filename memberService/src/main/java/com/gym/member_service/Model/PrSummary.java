package com.gym.member_service.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pr_summary")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PrSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private int year;

    @Column(nullable = false)
    private int month;

    @Column(name = "workout_name", nullable = false)
    private String workoutName;

    private Double avgWeight;
    private Integer avgReps;
    private Double maxWeight;
    private Integer maxReps;

    private Integer entryCount;
}
