package com.gym.trainerService.Repositories;

import com.gym.trainerService.Models.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member,String> {
}
