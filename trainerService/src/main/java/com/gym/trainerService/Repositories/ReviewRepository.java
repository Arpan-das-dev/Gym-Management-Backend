package com.gym.trainerService.Repositories;

import com.gym.trainerService.Models.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Session,String > {
}
