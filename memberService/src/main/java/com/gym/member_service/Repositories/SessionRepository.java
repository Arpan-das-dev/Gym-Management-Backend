package com.gym.member_service.Repositories;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.gym.member_service.Model.Session;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface SessionRepository extends JpaRepository<Session,String>{

    /**
     * Retrieves past sessions for a member.
     * Past = sessionStartTime < NOW()
     * Sorting is fully controlled by Pageable.
     */
    @Query("""
        SELECT s FROM Session s
        WHERE s.memberId = :memberId
          AND s.sessionStartTime <:now
    """)
    Page<Session> findPastSessionsByMemberId(
            @Param("memberId") String memberId,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );


    /**
     * Retrieves upcoming sessions for a member.
     * Upcoming = sessionStartTime >= NOW()
     * Sorting is fully controlled by Pageable.
     */
    @Query("""
        SELECT s FROM Session s
        WHERE s.memberId = :memberId
          AND s.sessionStartTime >=:now
    """)
    Page<Session> findUpcomingSessionsByMemberId(
            @Param("memberId") String memberId,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );


    /**
     * Retrieves sessions starting soon — NOT pageable.
     * Can be kept sorted in JPQL.
     */
    @Query("""
        SELECT s FROM Session s
        WHERE s.sessionStartTime BETWEEN :now AND :threshold
        ORDER BY s.sessionStartTime ASC
    """)
    List<Session> findSessionsStartingSoon(
            @Param("now") LocalDateTime now,
            @Param("threshold") LocalDateTime threshold
    );

}
