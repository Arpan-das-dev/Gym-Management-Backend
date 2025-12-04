package com.gym.adminservice.Repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import com.gym.adminservice.Models.MemberRequest;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemberRequestRepository extends JpaRepository<MemberRequest,String> {

    @Query("SELECT m FROM MemberRequest m WHERE m.memberId =:memberId")
    Optional<MemberRequest> findBYMemberId(@Param("memberId") String memberId);
}
