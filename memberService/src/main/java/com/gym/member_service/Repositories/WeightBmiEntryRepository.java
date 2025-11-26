package com.gym.member_service.Repositories;

import com.gym.member_service.Model.WeightBmiEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
public interface WeightBmiEntryRepository extends JpaRepository<WeightBmiEntry, Long> {

    Optional<WeightBmiEntry> findByMemberIdAndDate(String memberId, LocalDate date);

    int deleteByMember_IdAndDate(String memberId, LocalDate date);

    /*
     * a custom method(definition) with custom query to find member with data
     * between certain date range
     */
    @Query("SELECT w FROM WeightBmiEntry w WHERE w.member.id = :memberId ORDER BY w.date DESC")
    Page<WeightBmiEntry> findWeightBmiEntryByPages(@Param("memberId") String memberId, Pageable pageable);


    @Query(value = "SELECT * FROM memberservice.weight_bmi_entries " +
            "WHERE member_id = :memberId " +
            "AND date BETWEEN :endDate AND :startDate",
            nativeQuery = true)
    List<WeightBmiEntry> findAllByMemberIdAndWeek(@Param("memberId") String memberId,
                                                  @Param("endDate") LocalDate endDate,
                                                  @Param("startDate") LocalDate startDate);
}
