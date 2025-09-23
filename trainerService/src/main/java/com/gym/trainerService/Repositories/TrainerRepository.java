package com.gym.trainerService.Repositories;

import com.gym.trainerService.Models.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerRepository extends JpaRepository<Trainer,String > {
    boolean existsByEmail( String email);
}
