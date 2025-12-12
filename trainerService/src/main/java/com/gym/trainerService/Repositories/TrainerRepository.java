package com.gym.trainerService.Repositories;

import com.gym.trainerService.Models.Trainer;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TrainerRepository extends JpaRepository<Trainer,String > {
    boolean existsByEmail( String email);

    @Modifying
    @Transactional
    @Query("UPDATE Trainer t SET t.averageRating = :avg WHERE t.trainerId = :id")
    int updateAverageRating(@Param("id") String id, @Param("avg") double avg);

}
