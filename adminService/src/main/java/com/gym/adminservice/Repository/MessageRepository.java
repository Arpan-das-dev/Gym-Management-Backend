package com.gym.adminservice.Repository;

import com.gym.adminservice.Models.Messages;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MessageRepository extends JpaRepository<Messages,String> {
    @Query("SELECT m FROM Messages m WHERE userId = :userId")
    List<Messages> findAllByUserId(@Param("userId") String userId);
}
