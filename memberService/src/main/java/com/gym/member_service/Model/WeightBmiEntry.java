package com.gym.member_service.Model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "weight_bmi_entries",
        uniqueConstraints = @UniqueConstraint(columnNames = { "member_id", "date" }))
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WeightBmiEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @EqualsAndHashCode.Exclude
    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Double weight;

    @Column(nullable = false)
    private Double bmi;
}
