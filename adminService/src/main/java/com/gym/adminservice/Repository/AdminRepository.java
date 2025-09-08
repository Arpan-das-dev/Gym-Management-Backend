package com.gym.adminservice.Repository;

import com.gym.adminservice.Models.AdminEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<AdminEntity,String > {

    Optional<AdminEntity> findByEmail(String email);
    void deleteByEmail(String email);
}
