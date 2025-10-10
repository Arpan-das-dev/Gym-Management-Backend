package com.gym.trainerService.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a review submitted by a user for a trainer.
 * <p>
 * Stores the review text, rating, vote count, and whether the review has been reported.
 * Each review is linked to a trainer using a Many-to-One relationship.
 * </p>
 *
 * <p><b>Indexes:</b></p>
 * <ul>
 *     <li>userId: for efficient lookups of reviews by a specific user</li>
 *     <li>trainer_id: for fetching all reviews of a trainer quickly</li>
 *     <li>trainer_id + userId: for checking if a user has already reviewed a trainer</li>
 *     <li>reported: optional, for filtering flagged reviews efficiently</li>
 * </ul>
 *
 * @author Arpan Das
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "reviews", indexes = {
        @Index(name = "idx_user_id", columnList = "userId"),
        @Index(name = "idx_trainer_id", columnList = "trainerId"),
        @Index(name = "idx_trainer_user", columnList = "trainerId, userId"),
        @Index(name = "idx_reported", columnList = "reported"),
        @Index(name = "idx_helpFull_vote", columnList = "helpFullVote")
})
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String reviewId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false)
    private String userRole;

    @Column(nullable = false)
    private String comment;

    @Column(nullable = false)
    private Double review = 0.0;

    @Column(nullable = false)
    private int helpFullVote = 0;

    @Column(nullable = false)
    private int notHelpFullVote = 0;

    private boolean reported = false;

    private LocalDateTime reviewDate;

    /**
     * Many-to-One relationship to the Trainer entity.
     * Fetch type is LAZY to avoid loading the trainer unless explicitly needed.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id")
    private Trainer trainer;
}
