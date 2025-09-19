package com.gym.member_service.Model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Entity representing a Trainer assigned to a Member.
 * <p>
 * This entity stores the relationship between a member and their trainer,
 * including trainer details and the eligibility period for the trainer assignment.
 * </p>
 */
@Entity
@Table(name = "trainers", indexes = {
        @Index(name = "idx_member_id", columnList = "memberId"),
        @Index(name = "idx_trainer_id", columnList = "trainerId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trainer {

    /**
     * Primary key for Trainer entity.
     * Usually auto-generated.
     */
    @Id
    @Column(nullable = false)
    private String trainerId;

    /**
     * Full name of the trainer.
     */
    @Column(nullable = false)
    private String trainerName;

    /**
     * Profile image URL of the trainer.
     */
    @Column(name = "trainer_url")
    private String trainerProfileImageUrl;

    /**
     * The ID of the member this trainer is assigned to.
     */
    @Column(nullable = false)
    private String memberId;


    /**
     * Date until when the trainer is eligible to train this member.
     */
    @Column(nullable = false)
    private LocalDate eligibilityEnd;
}
