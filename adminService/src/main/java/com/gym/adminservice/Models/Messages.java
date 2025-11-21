package com.gym.adminservice.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_message_time", columnList = "message_time"),
        @Index(name = "idx_message_time", columnList = "status")
})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Messages {

    @Id
    private String messageId;
    @Column(nullable = false,name = "subject")
    private String subject;
    @Column(nullable = false, name = "user_id")
    private String userId;
    @Column(nullable = false, name = "user_name")
    private String userName;
    @Column(nullable = false,name = "user_Role")
    private String userRole;
    @Column(nullable = false,name = "email")
    private String emailId;
    @Column(nullable = false,name = "message")
    private String message;
    @Column(nullable = false, name = "status")
    private String status;
    @Column(nullable = false, name = "message_time")
    private LocalDateTime messageTime;
}
