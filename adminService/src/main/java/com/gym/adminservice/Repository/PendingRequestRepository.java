package com.gym.adminservice.Repository;

import com.gym.adminservice.Models.PendingRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PendingRequestRepository extends JpaRepository<PendingRequest, String> {
    void deleteByEmail(String email);

    Optional<PendingRequest> findByEmail(String email);

    boolean existsByEmail(String email);
}
