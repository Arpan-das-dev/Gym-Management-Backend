package com.gym.member_service.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "membersActive")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MembersActive {
    @Id
    private LocalDateTime dateTime;

    @Column(nullable = false)
    private String time;
    private long memberCount;
}
