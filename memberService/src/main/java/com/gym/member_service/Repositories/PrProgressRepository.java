package com.gym.member_service.Repositories;

import com.gym.member_service.Model.Member;
import com.gym.member_service.Model.PrProgresses;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
            " AND achieved_date BETWEEN :endDate AND :startDate",
            nativeQuery = true)
    List<PrProgresses> findByMemberIdAndDateRange(@Param("memberId") String memberId,
                                                  @Param("endDate") LocalDate endDate,
                                                  @Param("startDate") LocalDate startDate);

    /*
     * a custom method(definition) with custom query to find member with data
     * on a certain dateFL
     */
    @Modifying
    @Query("delete  FROM PrProgresses p " +
            "WHERE p.member.id = :memberId " +
            "and p.achievedDate = :date")
    int deleteByMemberIdAndDate(@Param("memberId") String memberId, @Param("date") LocalDate date);

    /*
     * a custom method(definition) with custom query to find member with data
     * on a certain date with work out name
     */
    @Modifying
    @Query("DELETE FROM  PrProgresses p " +
            "WHERE p.member.id = :memberId " +
            "and p.achievedDate = :date " +
            "and p.workoutName = :workoutName")
    int deletePrByMemberIdWithDateAndName(@Param("memberId") String memberId,
                                          @Param("date") LocalDate date,
                                          @Param("workoutName") String workoutName);

    @Query("SELECT p FROM PrProgresses p " +
            "WHERE p.member.id = :memberId " +
            "AND achievedDate BETWEEN :startDate AND :endDate")
    List<PrProgresses> findAllByMemberIdAndWeek(@Param("memberId") String memberId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate
                                                );

    @Query("SELECT p FROM PrProgresses p WHERE p.member.id = :memberId" +
            " AND p.workoutName = :workoutName AND p.achievedDate = :achievedDate")
    Optional<PrProgresses> findByWorkoutAndDate(
            @Param("memberId") String memberId,
            @Param("workoutName") String workoutName,
            @Param("achievedDate") LocalDate achievedDate);

    @Query("""
            SELECT p FROM PrProgresses p
            WHERE p.member = :member
            AND (:searchBy IS NULL OR :searchBy = ''
                  OR LOWER(p.workoutName) LIKE LOWER(CONCAT('%', :searchBy, '%')))
            AND (:from IS NULL OR :to IS NULL OR p.achievedDate BETWEEN :from AND :to)
            """)
    Page<PrProgresses> findForMemberBySearchDirectionWithDateRangeAndPage(
            @Param("member") Member member,
            @Param("searchBy") String searchBy,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            Pageable page
    );

}
