package com.gym.member_service.Repositories;

import com.gym.member_service.Model.BmiSummary;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface BmiSummaryRepository extends JpaRepository<BmiSummary,Long> {
    List<BmiSummary> findByMemberId(String memberId);

}
