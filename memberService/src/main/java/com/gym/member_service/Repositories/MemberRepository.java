package com.gym.member_service.Repositories;

import com.gym.member_service.Model.Member;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, String> {

        @EntityGraph(attributePaths = { "weightBmiEntries", "prProgresses", "dailyRoutines.exercises" })
        Optional<Member> findWithDetailsById(String id);

        // decrease the duration of members plan left duration by 1 if the current
        // duration is less than -10
        @Modifying
        @Transactional
        @Query(value = "UPDATE members  SET plan_duration_left = plan_duration_left-1 " +
                        "WHERE plan_duration_left>= -10", nativeQuery = true)
        int decrementDurationForAllMembers();

        // when the plan expires it will set the all details about plan as null
        @Modifying
        @Transactional
        @Query(value = "UPDATE members  SET plan_name = null, planid = null, active_plan = false " +
                        "WHERE plan_expiration < :now", nativeQuery = true)
        int expirePlan(@Param("now") LocalDateTime now);

        // frozen the account which plan duration is less than -10
        @Modifying
        @Transactional
        @Query(value = "UPDATE members  SET frozen = true " +
                        "WHERE plan_duration_left <=- 10", nativeQuery = true)
        int freezeExpiredAccounts();

        // select all those members whose account is frozen
        @Query(value = "SELECT * FROM members " +
                        "WHERE frozen = true", nativeQuery = true)
        List<Member> getFrozenMemberList();

        // returns an integer value of members with specific plan name
        @Query(value = "SELECT count(*) FROM members " +
                        "WHERE plan_name = :name", nativeQuery = true)
        int memberCountWithPlansNameOf(@Param("name") String planName);

        // return a list of members by specific name
        @Query(value = "SELECT * FROM members " +
                        "WHERE plan_name = :name", nativeQuery = true)
        List<Member> getMemberListByPlanName(@Param("name") String name);

        // return a list members whose plan duration is left for certain days (as
        // parameter)
        @Query(value = "SELECT * FROM members " +
                        "WHERE plan_duration_left = :duration", nativeQuery = true)
        List<Member> getMemberListByDuration(@Param("duration") int duration);
}
