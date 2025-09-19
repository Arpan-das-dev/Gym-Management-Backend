package com.gym.member_service.Repositories;

import com.gym.member_service.Model.Trainer;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TrainerRepository extends JpaRepository<Trainer,Long> {
    /**
     * Finds the trainer currently assigned to a given member.
     *
     * @param memberId ID of the member
     * @return Optional containing Trainer if found, otherwise empty
     */
    @Query("SELECT t FROM Trainer t WHERE t.memberId = :memberId")
    Optional<Trainer> findTrainerByMemberId(@Param("memberId") String memberId);
}
