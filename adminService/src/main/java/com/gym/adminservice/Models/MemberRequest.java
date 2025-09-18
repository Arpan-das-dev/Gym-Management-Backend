package com.gym.adminservice.Models;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "memberRequests", indexes = {
        @Index(name = "idx_member_id", columnList = "memberId"),
        @Index(name = "idx_trainer_id", columnList = "trainerId"),
        @Index(name = "idx_session_start_time", columnList = "requestDate"),
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberRequest {
    @Id
    private String requestId;

    @Column( nullable = false)
    private String memberId;

    @Column( nullable = false)
    private String memberName;

    @Column(name = "member_url")
    private String memberProfileImageUrl;

    @Column( nullable = false)
    private String trainerId;

    @Column(name = "trainer_url")
    private String trainerProfileImageUrl;

    @Column(nullable = false)
    private String trainerName;

    @Column(nullable = false)
    private LocalDate requestDate;

    @Column(nullable = false)
    private String memberPlanName;

    private LocalDate memberPlanExpirationDate;
}
