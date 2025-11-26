package com.gym.member_service.Repositories;

import com.gym.member_service.Model.BmiSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.time.LocalDate;
import java.util.List;

public interface BmiSummaryRepository extends JpaRepository<BmiSummary,Long> {

    Page<BmiSummary> findByMemberId(String memberId, Pageable pageable);
}
