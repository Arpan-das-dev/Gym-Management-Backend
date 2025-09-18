package com.gym.member_service.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bmi_summary")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BmiSummary {
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

    private Double avgBmi;
    private Double minBmi;
    private Double maxBmi;

    private Double avgWeight;
    private Double minWeight;
    private Double maxWeight;

    private Integer entryCount;
}
