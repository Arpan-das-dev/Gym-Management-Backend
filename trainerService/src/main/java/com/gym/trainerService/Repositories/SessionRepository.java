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
/**
 * Repository interface for managing {@link Session} entities.
 * <p>
 * Extends {@link JpaRepository} to provide basic CRUD operations and
 * adds custom JPQL queries to handle session scheduling, pagination,
 * and slot validation for trainers.
 * </p>
 *
 * <p><b>Responsibilities:</b></p>
 * <ul>
 *   <li>Retrieve upcoming and past sessions for a trainer.</li>
 *   <li>Support pagination for past session history.</li>
 *   <li>Check for overlapping session slots to prevent scheduling conflicts.</li>
 * </ul>
 *
 * <p><b>Author:</b> Arpan</p>
 * @since 1.0
 */
public interface SessionRepository extends JpaRepository<Session,String > {

    /**
     * Retrieves all upcoming sessions for a specific trainer.
     * <p>
     * Fetches sessions where the session start time is greater than or equal to the current time,
     * effectively returning all future sessions. Results are ordered chronologically.
     * </p>
     *
     * @param trainerId   unique identifier of the trainer
     * @param currentTime current timestamp used to filter future sessions
     * @return list of {@link Session} entities representing upcoming sessions
     */
    @Query("""
       SELECT s FROM Session s
         WHERE s.trainerId = :trainerId
         AND s.sessionStartTime >= :currentTime
         ORDER BY s.sessionStartTime ASC
       """)
    List<Session> findByTrainerId(@Param("trainerId") String trainerId,
                                  @Param("currentTime") LocalDateTime currentTime);


    /**
     * Retrieves past sessions for a specific trainer in paginated form.
     * <p>
     * Fetches sessions that occurred before the provided timestamp,
     * ordered in descending order by start time (most recent first).
     * Useful for displaying session history with pagination controls.
     * </p>
     *
     * @param trainerId   unique identifier of the trainer
     * @param currentTime timestamp used to determine “past” sessions
     * @param pageRequest {@link Pageable} configuration defining page size and number
     * @return a {@link Page} containing {@link Session} entities
     */
    @Query("""
       SELECT s FROM Session s
         WHERE s.trainerId = :trainerId
         AND s.sessionStartTime < :currentTime
         ORDER BY s.sessionStartTime DESC
       """)
    Page<Session> findPaginatedDataByTrainerId(@Param("trainerId") String trainerId,
                                               @Param("currentTime") LocalDateTime currentTime,
                                               Pageable pageRequest);

    /**
     * Checks if a session slot overlaps with an existing session.
     * <p>
     * Used during session creation or update to ensure the new session
     * does not conflict with other sessions occurring within the same timeframe.
     * </p>
     *
     * @param startTime proposed session start time
     * @param endTime   proposed session end time
     * @return an {@link Optional} containing an existing {@link Session} if a conflict exists,
     *         or an empty {@link Optional} if the slot is available
     */
    @Query("""
       SELECT s FROM Session s
         WHERE s.sessionStartTime >= :startTime
         AND s.sessionEndTime <= :endTime
       """)
    Optional<Session> sessionSlotCheck(@Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime);

    @Query("SELECT s FROM Session s WHERE s.sessionStartTime BETWEEN :startOfWeek AND :endOfWeek")
    List<Session> sessionInWeekRange(
            @Param("startOfWeek") LocalDateTime startOfWeek,
            @Param("endOfWeek") LocalDateTime endOfWeek);
}
