package com.gym.authservice.Repository;

import com.gym.authservice.Entity.SignedUps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface SignedUpsRepository extends JpaRepository<SignedUps,String > {
    boolean existsByEmail(String email);

   Optional< SignedUps> findByEmail(String username);
}
