package com.gym.trainerService.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "specialities",indexes = {
        @Index(name = "idx_trainer_id",columnList = "trainerId"),
        @Index(name = "idx_speciality",columnList = "speciality")
})
public class Specialities {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String trainerId;

    @Column(nullable = false)
    private String speciality;
}
