package com.gym.trainerService.Repositories;

import com.gym.trainerService.Models.Specialities;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Trainer Specialities.
 * <p>
 * This repository provides CRUD operations as well as custom queries
 * to fetch, update, and delete specialities associated with trainers.
 * All queries are based on trainer ID and speciality name.
 * </p>
 *
 * <p>Extends {@link JpaRepository} to leverage Spring Data JPA methods
 * for persistence and transaction management.</p>
 *
 * <p>Custom queries use JPQL to avoid hardcoding schema names,
 * making the repository more portable across different environments.</p>
 *
 * @author Arpan Das
 * @version 1.0
 * @since 1.0
 */
public interface SpecialityRepository extends JpaRepository<Specialities,String > {
    /**
     * Fetches all specialities for a given trainer.
     *
     * @param trainerId the unique identifier of the trainer
     * @return a {@link List} of {@link Specialities} for the trainer,
     *         empty list if none found
     * @see Specialities
     */
    @Query("SELECT s FROM Specialities s WHERE s.trainerId = :trainerId")
    List<Specialities> findByTrainerId(@Param("trainerId") String trainerId);

    /**
     * Finds a specific speciality by trainer ID and speciality name.
     * <p>
     * Returns the matching {@link Specialities} entity if found; otherwise null.
     * Using Optional could also be considered to reduce risk of NPE.
     * </p>
     *
     * @param trainerId      the unique identifier of the trainer
     * @param specialityName the name of the speciality to search
     * @return {@link Specialities} entity matching the trainer and speciality name, or null if not found
     * @see Specialities
     */
    @Query("SELECT s FROM Specialities s WHERE s.trainerId = :trainerId AND s.speciality = :specialityName")
    Specialities findSpecialityByTrainerIdAndName(@Param("trainerId") String trainerId,
                                                  @Param("specialityName") String specialityName);

    /**
     * Deletes a speciality for a trainer by trainer ID and speciality name.
     * <p>
     * The number of rows affected is returned for logging or validation purposes.
     * This method is annotated with {@link Modifying} to indicate a write operation.
     * </p>
     *
     * @param trainerId      the unique identifier of the trainer
     * @param specialityName the name of the speciality to delete
     * @return the number of rows affected by the delete operation
     * @see Modifying
     */
    @Modifying
    @Query("DELETE FROM Specialities s WHERE s.trainerId = :trainerId AND s.speciality = :specialityName")
    int deleteByTrainerIdWithName(@Param("trainerId") String trainerId,
                                  @Param("specialityName") String specialityName);


    @Query("SELECT s FROM Specialities s WHERE s.trainerId =:trainerId")
    List<Specialities> findAllTrainerId(@Param("trainerId") String trainerId);

    @Query("SELECT COUNT(s) FROM Specialities s WHERE s.trainerId = :trainerId")
    int getSpecialityCount(@Param("trainerId") String trainerId);
}
