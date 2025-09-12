package com.gym.member_service.Model;

import java.time.LocalDate;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "daily_routines")
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class DailyRoutine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @EqualsAndHashCode.Exclude
    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "routine_date", nullable = false)
    private LocalDate routineDate;

    @Column(name ="day", nullable = false)
    private String day;

    @Column(name = "exercises")
    @OneToMany(mappedBy = "dailyRoutine", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private Set<Exercise> exercises;
}
