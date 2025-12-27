package com.gym.planService.Repositories;

import com.gym.planService.Models.PlanPayment;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PlanPaymentRepository extends JpaRepository<PlanPayment,String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PlanPayment p WHERE p.orderId = :orderId")
    Optional<PlanPayment> findByOrderId(@Param("orderId") String orderId);

    @Query("SELECT SUM(p.paidPrice) FROM PlanPayment p " +
            "WHERE p.paymentStatus = 'SUCCESS' AND p.paymentMonth = :month AND p.paymentYear = :year")
    Optional<Double> sumRevenueByMonthAndYear(@Param("month") String month,
                                              @Param("year") Integer year);

    @Query("SELECT p FROM PlanPayment p " +
            "WHERE p.paymentStatus = 'SUCCESS' AND p.paymentMonth =:month AND p.paymentYear = :year")
    List<PlanPayment> findByCurrentMonthAndYear(@Param("month") String month,
                                                @Param("year") Integer year);

    @Query("""
        SELECT p FROM PlanPayment p
        WHERE LOWER(p.userName) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(p.userId) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(p.planId) LIKE LOWER(CONCAT('%', :search, '%'))
           OR CAST(p.paymentDate AS string) LIKE CONCAT('%', :search, '%')
           OR CAST(p.transactionTime AS string) LIKE CONCAT('%', :search, '%')
    """)
    Page<PlanPayment> searchByUserNameOrDate(@Param("search") String search, Pageable pageable);

    @Query("SELECT p FROM PlanPayment p WHERE p.userId = :userId " +
            "AND (:status = 'ALL' OR p.paymentStatus = :status)")
    Page<PlanPayment> findReceiptCustomUsers(
            @Param("userId") String userId,
            @Param("status") String status,
            Pageable pageable
    );

    @Query("""
            SELECT
                p.paymentYear,
                p.paymentMonth,
                SUM(p.paidPrice)
            FROM PlanPayment p
            WHERE p.paymentYear = :year
              AND p.paymentStatus = 'SUCCESS'
            GROUP BY p.paymentYear, p.paymentMonth
            ORDER BY
              CASE p.paymentMonth
                WHEN 'JANUARY' THEN 1
                WHEN 'FEBRUARY' THEN 2
                WHEN 'MARCH' THEN 3
                WHEN 'APRIL' THEN 4
                WHEN 'MAY' THEN 5
                WHEN 'JUNE' THEN 6
                WHEN 'JULY' THEN 7
                WHEN 'AUGUST' THEN 8
                WHEN 'SEPTEMBER' THEN 9
                WHEN 'OCTOBER' THEN 10
                WHEN 'NOVEMBER' THEN 11
                WHEN 'DECEMBER' THEN 12
              END
            """)
    List<Object[]> findMonthlyRevenueByYear(@Param("year") int year);

    @Query("SELECT p FROM PlanPayment p ORDER BY p.paymentDate ASC")
    List<PlanPayment> findOldestTransaction(Pageable pageable);

    @Query("SELECT p FROM PlanPayment p ORDER BY p.paymentDate DESC")
    List<PlanPayment> findNewestTransaction(Pageable pageable);

    @Query("""
                SELECT
                    p.planId,
                    p.planName,
                    SUM(p.paidPrice),
                    COUNT(p.planId)
                FROM PlanPayment p
                WHERE p.paymentStatus = 'SUCCESS'
                GROUP BY p.planId, p.planName
                ORDER BY SUM(p.paidPrice) DESC
            """)
    List<Object[]> findLifetimeIncomePerPlan();


    @Query("""
            SELECT
                p.planId,
                p.planName,
                SUM(p.paidPrice),
                COUNT(p.planId)
            FROM PlanPayment p
            WHERE p.paymentStatus = 'SUCCESS'
            AND p.planId = :planId
            GROUP BY p.planId, p.planName
            """)
    Object[] findLifeTimeIncomeByPlanId(@Param("planId") String planId);

    @Query("SELECT SUM(p.paidPrice) FROM PlanPayment p WHERE p.paymentStatus = 'SUCCESS'")
    Double getLifeTimeIncome();

    @Query("""
                SELECT
                    SUM(CASE WHEN p.paymentYear = :year THEN p.paidPrice ELSE 0 END),
                    SUM(CASE WHEN p.paymentYear = :year AND p.paymentMonth = :month THEN p.paidPrice ELSE 0 END)
                FROM PlanPayment p
                WHERE p.paymentStatus = 'SUCCESS'
            """)
    Object[] findIncomeByYearAndMonth(
            @Param("year") int year,
            @Param("month") String month
    );


    @Query("""
                SELECT SUM(p.paidPrice)
                FROM PlanPayment p
                WHERE p.paymentStatus = 'SUCCESS'
                  AND p.transactionTime BETWEEN :start AND :end
            """)
    Double findTodayIncome(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Modifying
    @Query("""
                UPDATE PlanPayment p
                SET p.planName = :newName
                WHERE p.planId = :planId AND p.planName = :oldName
            """)
    int updatePlanName(
            @Param("planId") String planId,
            @Param("oldName") String oldName,
            @Param("newName") String newName
    );
}
