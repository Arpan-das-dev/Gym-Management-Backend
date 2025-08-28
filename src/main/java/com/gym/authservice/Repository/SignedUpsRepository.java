package com.gym.authservice.Repository;

import com.gym.authservice.Entity.SignedUps;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SignedUpsRepository extends JpaRepository<SignedUps,String > {
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
   Optional< SignedUps> findByEmail(String username);

    Optional<SignedUps> findByPhone(String key);
}
