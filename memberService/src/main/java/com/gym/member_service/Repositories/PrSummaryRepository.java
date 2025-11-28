package com.gym.member_service.Repositories;

import com.gym.member_service.Model.Member;
import com.gym.member_service.Model.PrSummary;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface PrSummaryRepository extends JpaRepository<PrSummary,Long> {


    List<PrSummary> findAllByMemberAndYearAndMonthAndWorkoutNameIn(Member member, int summaryYear, int summaryMonth, List<String> workoutNamesToSummarize);

    @Query("SELECT p FROM PrSummary p " +
            "WHERE p.member = :member " +
            "AND (:searchBy IS NULL OR :searchBy = '' OR " +
            "LOWER(p.workoutName) LIKE LOWER(CONCAT('%', :searchBy, '%'))) " +
            "AND (:from IS NULL OR :to IS NULL OR ( " +
            " (p.year > FUNCTION('YEAR', :from) OR (p.year = FUNCTION('YEAR', :from) " +
            " AND p.month >= FUNCTION('MONTH', :from))) " +
            " AND (p.year < FUNCTION('YEAR', :to) OR (p.year = FUNCTION('YEAR', :to) " +
            " AND p.month <= FUNCTION('MONTH', :to)))" +
            "))"
    )
    Page<PrSummary> findForMemberBySearchDirectionWithDateRangeAndPage(
            @Param("member") Member member,
            @Param("searchBy") String searchBy,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            Pageable page);
}
