package com.gym.member_service.Repositories;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.gym.member_service.Model.Session;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SessionRepository extends JpaRepository<Session,String>{

    @Query(value = "SELECT * FROM session s " +
            "WHERE s.member_id = :memberId " +
            "AND s.start_time < NOW() " +
            "ORDER BY s.start_time DESC",
            countQuery = "SELECT COUNT(*) FROM session s " +
                    "WHERE s.member_id = :memberId " +
                    "AND s.start_time < NOW()",
            nativeQuery = true)
    Page<Session> findPastSessionsByMemberId(@Param("memberId") String memberId, Pageable pageable);

    @Query(value = "SELECT * FROM session s " +
            "WHERE s.member_id = :memberId " +
            "AND s.start_time >= NOW() " +
            "ORDER BY s.start_time ASC",
            nativeQuery = true)
    List<Session> findUpcomingSessionsByMemberId(@Param("memberId") String memberId);


}
