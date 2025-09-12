package com.gym.member_service.Repositories;

import com.gym.member_service.Model.DailyRoutine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyRoutineRepository  extends JpaRepository<DailyRoutine, Long> {
}
