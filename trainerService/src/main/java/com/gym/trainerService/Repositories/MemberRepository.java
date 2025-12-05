package com.gym.trainerService.Repositories;

import com.gym.trainerService.Models.Member;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link Member} entities.
 * <p>
 * Provides custom query methods to fetch and delete members
 * based on their associated trainer.
 * </p>
 *
 * <p><b>Indexes:</b> Optimized queries using indexes on {@code memberId} and {@code trainerId}
 * ensure efficient lookup and deletion operations.</p>
 *
 * @author Arpan
 * @version 1.0
 * @since 1.0
 */
public interface MemberRepository extends JpaRepository<Member, String> {

    /**
     * Fetches all members assigned to a specific trainer.
     *
     * @param trainerId ID of the trainer whose members are to be retrieved.
     * @return List of {@link Member} entities linked to the provided trainer ID.
     */
    @Query("SELECT m FROM Member m WHERE m.trainerId = :trainerId")
    List<Member> findByTrainerId(@Param("trainerId") String trainerId);

    /**
     * Deletes a member entry for a given trainer.
     * <p>
     * This method performs a direct database-level delete operation
     * without fetching entities into persistence context.
     * </p>
     *
     * @param trainerId ID of the trainer.
     * @param memberId  ID of the member to be deleted.
     * @return Number of rows affected (0 if no record was found).
     */
    @Modifying
    @Query("DELETE FROM Member m WHERE m.trainerId = :trainerId AND m.memberId = :memberId")
    int deleteByTrainerAndMember(@Param("trainerId") String trainerId,
                                 @Param("memberId") String memberId);

    @Query("SELECT m FROM Member m WHERE m.trainerId = :trainerId AND m.memberId = :memberId")
    Optional<Member> findByTrainerIdMemberId(@Param("trainerId") String trainerId,
                                     @Param("memberId") String memberId);

    @Query("SELECT c.trainerId, COUNT(c) FROM Member c GROUP BY c.trainerId")
    List<Object[]> getClientCountForAllTrainers();

    /**
     * JPQL Method 1: Counts members currently eligible.
     * @param currentDate The current date (e.g., 2025-12-05).
     */
    @Query("SELECT COUNT(m) FROM Member m WHERE m.eligibilityEnd >= :currentDate")
    Long countCurrentMembers(@Param("currentDate") LocalDate currentDate);

    /**
     * JPQL Method 2: Counts members eligible at the end of the last month.
     * @param lastMonthEndDate The last day of the previous month (e.g., 2025-11-30).
     */
    @Query("SELECT COUNT(m) FROM Member m WHERE m.eligibilityEnd >= :lastMonthEndDate")
    Long countMembersEligibleAtLastMonthEnd(@Param("lastMonthEndDate") LocalDate lastMonthEndDate);
}