package com.gym.member_service;

import com.gym.member_service.Model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Repository extends JpaRepository<Member,String> {

}
