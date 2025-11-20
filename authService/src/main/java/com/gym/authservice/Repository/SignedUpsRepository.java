package com.gym.authservice.Repository;

import com.gym.authservice.Entity.SignedUps;
import com.gym.authservice.Roles.RoleType;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Repository
public interface SignedUpsRepository extends ReactiveCrudRepository<SignedUps, String> {

    Mono<Boolean> existsByPhone(String phone);

    Mono<SignedUps> findByEmail(String email);

    Mono<SignedUps> findByPhone(String phone);

    Mono<Void> deleteByEmail(String email);

    Mono<Boolean> existsByEmail(String email);

    @Query("""
    INSERT INTO signed_up (
        gym_id, first_name, last_name, gender, user_mail, phone_no,
        password, role, joined_on, is_verified_email, is_verified_phone, approved
    ) VALUES ( :id, :firstName, :lastName, :gender, :email, :phone,
        :password, :role, :joinDate, :isEmailVerified, :isPhoneVerified, :approved
    )
""")
    Mono<Void> insertSignedUp(String id, String firstName, String lastName, String gender, String email,
                              String phone, String password, RoleType role, LocalDate joinDate,
                              boolean isEmailVerified, boolean isPhoneVerified, boolean approved);



}
