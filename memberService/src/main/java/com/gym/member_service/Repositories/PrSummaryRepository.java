package com.gym.member_service.Repositories;

import com.gym.member_service.Model.PrSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrRepositorySummary extends JpaRepository<PrSummary,Long> {
    List<PrSummary> findByMemberId(String memberId);
}
