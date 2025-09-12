package com.gym.member_service.Model;

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
@Table(name = "exercises")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Exercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @EqualsAndHashCode.Exclude
    @ManyToOne
    @JoinColumn(name = "daily_routine_id", nullable = false)
    private DailyRoutine dailyRoutine;

    @Column(name = "Workout_name", nullable = false)
    private String workoutName;

    @Column(name = "sets", nullable = false)
    private int sets;

    @Column(name = "repetitions", nullable = false)
    private int repetitions;

    @Column(name = "weight", nullable = false)
    private double weight;

    @Column(name = "volume", nullable = false)
    private double volume;

    public void calculateVolume() {
        this.volume = this.sets * this.repetitions * this.weight;
    }
}
