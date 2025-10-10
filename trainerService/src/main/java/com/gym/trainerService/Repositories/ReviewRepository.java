package com.gym.trainerService.Repositories;

import com.gym.trainerService.Models.Review;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
/**
 * Repository interface for managing {@link Review} entities.
 * <p>
 * This interface extends {@link JpaRepository} to provide standard CRUD operations
 * and defines custom JPQL queries for retrieving and aggregating reviews by trainer.
 * </p>
 *
 * <p><b>Responsibilities:</b></p>
 * <ul>
 *   <li>Fetch paginated and sorted reviews for a given trainer.</li>
 *   <li>Compute average rating for a trainer based on submitted reviews.</li>
 * </ul>
 *
 * <p><b>Usage:</b></p>
 * <pre>
 * {@code
 * Page<Review> reviews = reviewRepository.findReviewsByTrainerId("T123", pageable);
 * Double average = reviewRepository.getReviewsByTrainerId("T123");
 * }
 * </pre>
 *
 * <p><b>Note:</b> Queries reference the trainer using {@code r.trainer.id},
 * assuming a ManyToOne relationship exists between Review and Trainer entities.
 * </p>
 *
 * @author Arpan
 * @since 1.0
 */
public interface ReviewRepository extends JpaRepository<Review, String> {

    /**
     * Retrieves a paginated list of {@link Review} entities for the given trainer ID.
     * <p>
     * Supports sorting and pagination through the {@link Pageable} parameter.
     * Commonly used to fetch recent or most helpful reviews efficiently.
     * </p>
     *
     * @param trainerId unique identifier of the trainer
     * @param pageable  pagination and sorting information
     * @return {@link Page} of {@link Review} entities
     */
    @Query("SELECT r FROM Review r WHERE r.trainer.id = :trainerId")
    Page<Review> findReviewsByTrainerId(@Param("trainerId") String trainerId, Pageable pageable);

    /**
     * Calculates the average review rating for a specific trainer.
     * <p>
     * Returns {@code null} if no reviews exist for the trainer.
     * </p>
     *
     * @param trainerId unique identifier of the trainer
     * @return average rating as {@link Double}, or {@code null} if no reviews found
     */
    @Query("SELECT avg(r.review) FROM Review r WHERE r.trainer.id = :trainerId")
    Double getReviewsByTrainerId(@Param("trainerId") String trainerId);
}