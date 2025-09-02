package com.gym.authservice.Repository;

import com.gym.authservice.Entity.SignedUps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface SignedUpsRepository extends JpaRepository<SignedUps,String > {
    boolean existsByPhone(String phone);

    Optional<SignedUps> findByEmail(String username);

    Optional<SignedUps> findByPhone(String key);

    void deleteByEmail(String email);

    boolean existsByEmail( String email);
}
