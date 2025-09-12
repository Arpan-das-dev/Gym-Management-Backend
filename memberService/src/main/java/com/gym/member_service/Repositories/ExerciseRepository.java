package com.gym.member_service.Repositories;

import com.gym.member_service.Model.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
}
