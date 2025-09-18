package com.gym.adminservice.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gym.adminservice.Models.MemberRequest;

public interface MemberRequestRepository extends JpaRepository<MemberRequest,String> {
    
}
