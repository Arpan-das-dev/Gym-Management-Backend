package com.gym.member_service.Repositories;

import com.gym.member_service.Model.PrProgresses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/*
 * This repository(interface) is used in memberFitService
 * to provide essentials(custom) and non essential(inbuild) methods
 * to do desired operations
 * @Modifying: use to tell that this method makes some changes in database
 * @Query: use to add custom query to do database operations
 * @Param: use to tell that any word in the query is actually dynamic(as method parameter)
 */
public interface PrProgressRepository extends JpaRepository<PrProgresses, Long> {

    List<PrProgresses> findByMemberIdAndWorkoutNameInAndAchievedDateIn(
            String memberId,
            List<String> workoutNames,
            List<LocalDate> achievedDates);

    /*
     * a custom method(definition) with custom query to find member with data
     * between certain date range
     */
    @Query(value = "SELECT * FROM members.pr_progresses " +
            "WHERE member_id = :memberId " +
            " AND achieved_date BETWEEN :startDate AND :endDate",
            nativeQuery = true)
    List<PrProgresses> findByMemberIdAndDateRange(@Param("memberId") String memberId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    /*
     * a custom method(definition) with custom query to find member with data
     * on a certain date
     */
    @Modifying
    @Query(value = "delete  FROM members.pr_progresses " +
            "WHERE member_id = :memberId " +
            "and achieved_date = :date", nativeQuery = true)
    int deleteByMemberIdAndDate(@Param("memberId") String memberId, @Param("date") LocalDate date);

    /*
     * a custom method(definition) with custom query to find member with data
     * on a certain date with work out name
     */
    @Modifying
    @Query(value = "DELETE FROM members.pr_progresses " +
            "WHERE member_id = :memberId " +
            "and achieved_date = :date " +
            "and workout_name = :workoutName", nativeQuery = true)
    int deletePrByMemberIdWithDateAndName(@Param("memberId") String memberId,
                                          @Param("date") LocalDate date,
                                          @Param("workoutName") String workoutName);
}
