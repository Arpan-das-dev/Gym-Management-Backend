package com.gym.planService.Repositories;

import com.gym.planService.Models.PlanPayment;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.util.List;
import java.util.Optional;

public interface PlanPaymentRepository extends JpaRepository<PlanPayment,String> {

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



}
