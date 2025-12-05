package com.gym.member_service.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Session", indexes = {
        @Index(name = "idx_member_id", columnList = "memberId"),
        @Index(name = "idx_trainer_id", columnList = "trainerId"),
        @Index(name = "idx_session_start_time", columnList = "startTime"),
        @Index(name = "idx_session_end_time", columnList = "endTime")
})
public class Session {
    @Id
    private String sessionId;
    private String sessionName;

    @Column(name = "memberId", nullable = false)
    private String memberId;

    @Column(name = "trainerId", nullable = false)
    private String trainerId;

    @Column(name = "startTime", nullable = false)
    private LocalDateTime sessionStartTime;

    @Column(name = "endTime", nullable = false)
    private LocalDateTime sessionEndTime;

    private String sessionStatus;
}
