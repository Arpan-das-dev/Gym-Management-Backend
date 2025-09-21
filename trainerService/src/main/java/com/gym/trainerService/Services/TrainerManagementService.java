package com.gym.trainerService.Services;

import com.gym.trainerService.Dto.TrainerMangementDto.Requests.TrainerCreateRequestDto;
import com.gym.trainerService.Dto.TrainerMangementDto.Responses.AllTrainerResponseDto;
import com.gym.trainerService.Exception.DuplicateTrainerFoundException;
import com.gym.trainerService.Models.Trainer;
import com.gym.trainerService.Repositories.TrainerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerManagementService {

    private final TrainerRepository trainerRepository;

    @Transactional
    @CacheEvict(value = "AllTrainerCache", key = "All")
    public AllTrainerResponseDto createTrainer( TrainerCreateRequestDto requestDto) {
        boolean existence = trainerRepository.existsById(requestDto.getId());
        if (existence) throw new DuplicateTrainerFoundException("User already exists with id: "+ requestDto.getId());
        Trainer trainer = Trainer.builder()
                .trainerId(requestDto.getId())
                .firstName(requestDto.getFirstName()).lastName(requestDto.getLastName())
                .phone(requestDto.getPhone()).email(requestDto.getEmail())
                .gender(requestDto.getGender())
                .joinDate(requestDto.getJoinDate())
                .build();
        trainerRepository.save(trainer);
        log.info("Trainer saved with name {}",trainer.getFirstName()+" "+ trainer.getLastName());
        return AllTrainerResponseDto.builder()
                .id(trainer.getTrainerId())
                .firstName(trainer.getFirstName()).lastName(trainer.getLastName())
                .email(trainer.getEmail()).phone(trainer.getPhone())
                .gender(trainer.getGender())
                .averageRating(trainer.getAverageRating())
                .lastLoginTime(trainer.getLastLogin())
                .build();
    }


    public List<AllTrainerResponseDto> getAllTrainer() {
        List<Trainer> trainers = trainerRepository.findAll();
        List<AllTrainerResponseDto> responseDtoList = trainers.stream()
    }
}
