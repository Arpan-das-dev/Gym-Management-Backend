package com.gym.planService.Repositories;

import com.gym.planService.Models.MonthlyRevenue;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface RevenueRepository extends JpaRepository<MonthlyRevenue, LocalDate> {

    @Query("SELECT m FROM MonthlyRevenue m " +
            "WHERE m.currentYear = :year")
    List<MonthlyRevenue> findByCurrentYear(@Param("year") Integer year);

    @Query("SELECT m FROM MonthlyRevenue m ORDER BY m.currentYear DESC, m.month ASC")
    List<MonthlyRevenue> findPaginatedData(Pageable pageable);

    @Query("""
        SELECT mr.currentMonth, COUNT(mr)
        FROM MonthlyRevenue mr
        WHERE mr.currentMonth IN :months
        GROUP BY mr.currentMonth
    """)
    List<Object[]> findUserCountsByMonths(@Param("months") List<String> months);


}
