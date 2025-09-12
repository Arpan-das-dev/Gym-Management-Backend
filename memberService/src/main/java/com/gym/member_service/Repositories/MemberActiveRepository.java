package com.gym.member_service.Repositories;

import com.gym.member_service.Model.MembersActive;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface MemberActiveRepository extends JpaRepository<MembersActive, LocalDateTime> {
}
