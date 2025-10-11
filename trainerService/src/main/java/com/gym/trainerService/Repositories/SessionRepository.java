package com.gym.trainerService.Repositories;

import com.gym.trainerService.Models.Session;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session,String > {

    @Query("""
       SELECT s FROM Session s
         WHERE s.trainerId = :trainerId
         AND s.sessionStartTime >= :currentTime
         ORDER BY s.sessionStartTime ASC
       """)
    List<Session> findByTrainerId(@Param("trainerId") String trainerId,
                                  @Param("now") LocalDateTime now);

    @Query("SELECT s FROM Session s WHERE s.trainerId = :trainerId AND s.sessionStartTime < :currentTime ORDER BY s.sessionStartTime DESC")
    Page<Session> findPaginatedDataByTrainerId(@Param("trainerId") String trainerId,
                                               @Param("currentTime") LocalDateTime currentTime,
                                               Pageable pageRequest);

    @Query("SELECT s FROM Session s WHERE s.sessionStartTime >= :startTime AND s.sessionEndTime =< :endTime")
    Optional<Session> sessionSlotCheck(@Param("startTIme") LocalDateTime startTime,
                                       LocalDateTime endTime);
}
