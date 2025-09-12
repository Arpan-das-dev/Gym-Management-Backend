package com.gym.member_service.Repositories;

import com.gym.member_service.Model.WeightBmiEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeightBmiEntryRepository extends JpaRepository<WeightBmiEntry, Long> {
}
