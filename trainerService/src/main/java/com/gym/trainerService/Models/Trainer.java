package com.gym.trainerService.Models;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Entity
@Table(name = "trainer", indexes = {
        @Index(name = "idx_rating", columnList = "average_rating")
})
public class Trainer implements Serializable {
    @Id
    private String trainerId;

    @Builder.Default
    @Column(nullable = false)
    private String trainerProfileImageUrl="";

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false,unique = true)
    private String email;

    @Column(nullable = false,unique = true)
    private String phone;

    @Column(nullable = false)
    private String gender;

    @Builder.Default
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(nullable = false)
    private LocalDate joinDate = LocalDate.now();

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime lastLogin = LocalDateTime.now();

    @Builder.Default
    @Column(nullable = false)
    private double averageRating = 0.0;

    @OneToMany(mappedBy = "trainer",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;

    @Builder.Default
    @Column(name = "about",columnDefinition = "TEXT")
    private String about = "";

    @Builder.Default
    @Column(name = "frozen")
    private boolean frozen = false;
}
