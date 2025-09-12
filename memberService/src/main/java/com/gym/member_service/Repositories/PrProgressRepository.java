package com.gym.member_service.Repositories;

import com.gym.member_service.Model.PrProgresses;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrProgressRepository extends JpaRepository<PrProgresses, Long> {
}
