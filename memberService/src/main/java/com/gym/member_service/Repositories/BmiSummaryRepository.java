package com.gym.member_service.Repositories;

import com.gym.member_service.Model.BmiSummary;
import com.gym.member_service.Model.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.Optional;

public interface BmiSummaryRepository extends JpaRepository<BmiSummary,Long> {

    Page<BmiSummary> findByMemberId(String memberId, Pageable pageable);

    @Query("SELECT b FROM BmiSummary b WHERE b.member.id = :memberId AND b.month = :month")
    Optional<BmiSummary> existByIdAndMonth(@Param("memberId") String memberId,
                                           @Param("month") int month);

    Optional<BmiSummary> findByMemberAndYearAndMonth(Member member, int summaryYear, int summaryMonth);
}
