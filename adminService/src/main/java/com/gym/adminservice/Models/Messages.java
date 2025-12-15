package com.gym.adminservice.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "messages",
        indexes = {
                @Index(name = "idx_messages_user_id", columnList = "user_id"),
                @Index(name = "idx_messages_message_time", columnList = "message_time"),
                @Index(name = "idx_messages_status", columnList = "status"),
                @Index(name = "idx_messages_user_role", columnList = "user_role")
        }
)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Messages {

    @Id
    @Column(length = 64)
    private String messageId;

    @Column(nullable = false, length = 150)
    private String subject;

    @Column(nullable = false, name = "user_id", length = 64)
    private String userId;

    @Column(nullable = false, name = "user_name", length = 100)
    private String userName;

    @Column(nullable = false, name = "user_role", length = 50)
    private String userRole;

    @Column(nullable = false, name = "email")
    private String emailId;

    @Column(nullable = false, name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(nullable = false, name = "message_time")
    private LocalDateTime messageTime;
}
