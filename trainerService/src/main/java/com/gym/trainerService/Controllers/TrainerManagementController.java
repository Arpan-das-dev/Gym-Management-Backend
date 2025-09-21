package com.gym.trainerService.Controllers;

import com.gym.trainerService.Dto.TrainerMangementDto.Requests.TrainerCreateRequestDto;
import com.gym.trainerService.Dto.TrainerMangementDto.Responses.AllTrainerResponseDto;
import com.gym.trainerService.Services.TrainerManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("${trainer-service.Base_Url}")
@Validated
public class TrainerManagementController {

    private final TrainerManagementService trainerManagementService;

    @PostMapping("/create")
    public ResponseEntity<AllTrainerResponseDto> createTrainer (@Valid @RequestBody TrainerCreateRequestDto requestDto) {
        log.info("Request received to create trainer with id {}", requestDto.getId());
        AllTrainerResponseDto responseDto = trainerManagementService.createTrainer(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping("/all/get")
    public ResponseEntity<TrainerReponseDto> getTrainerById(@RequestParam String trainerId){
        log.info("Request received to get info of trainer with id {}",trainerId);
        TrainerResponseDto responseDto = trainerManagementService.getTrainerById(trainerId);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @GetMapping("/admin/getById")
    public ResponseEntity<List<AllTrainerResponseDto>> getAllTrainer(){
        log.info("RequestReceived to get all trainerInfo");
        List<AllTrainerResponseDto> responseDto = trainerManagementService.getAllTrainer();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(responseDto);
    }
}
