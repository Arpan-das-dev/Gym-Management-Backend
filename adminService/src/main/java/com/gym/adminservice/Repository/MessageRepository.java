package com.gym.adminservice.Repository;

import com.gym.adminservice.Models.Messages;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MessageRepository extends JpaRepository<Messages,String> {
    @Query("SELECT m FROM Messages m WHERE m.userId = :userId")
    List<Messages> findAllByUserId(@Param("userId") String userId);

    @Query("SELECT count (m) FROM Messages m WHERE m.userId =:userId")
    int countByUserId(@Param("userId") String userId);

    @Query("""
                SELECT m FROM Messages m
                WHERE (:role = 'ALL' OR m.userRole = :role)
                  AND (:status = 'ALL' OR m.status = :status)
            """)
    Page<Messages> findAllWithFilters(
            @Param("role") String role,
            @Param("status") String status,
            Pageable pageable
    );
}
