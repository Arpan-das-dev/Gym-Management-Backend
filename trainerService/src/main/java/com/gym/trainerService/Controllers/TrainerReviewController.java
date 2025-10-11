package com.gym.trainerService.Controllers;

import com.gym.trainerService.Dto.TrainerReviewDto.Requests.ReviewAddRequestDto;
import com.gym.trainerService.Dto.TrainerReviewDto.Requests.ReviewUpdateRequestDto;
import com.gym.trainerService.Dto.TrainerReviewDto.Responses.ReviewResponseDto;
import com.gym.trainerService.Dto.TrainerReviewDto.Wrapper.AllReviewResponseWrapperDto;
import com.gym.trainerService.Services.TrainerServices.TrainerReviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("${trainer-service.Base_Url.Review}")
@RequiredArgsConstructor
@Validated
public class TrainerReviewController {

    private final TrainerReviewService trainerReviewService;

    @PostMapping("/add")
    public ResponseEntity<ReviewResponseDto> addReviewForMember (@RequestParam String trainerId,
                                                                 @Valid @RequestBody ReviewAddRequestDto requestDto) {
        log.info("Request received to  add review for trainer {} by {} ",trainerId,requestDto.getUserName());
        ReviewResponseDto response = trainerReviewService.addReviewForMemberByUser(trainerId,requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/getAll")
    public ResponseEntity<AllReviewResponseWrapperDto> getAllReviewByTrainerId(@RequestParam @NotBlank String trainerId,
                                                                               @RequestParam @PositiveOrZero int pageNo,
                                                                               @RequestParam @Positive int pageSize,
                                                                               @RequestParam @NotBlank String  sortBy,
                                                                               @RequestParam @NotBlank String sortDirection)
    {
        log.info("Request received to get all review for trainer id: {}",trainerId);
        AllReviewResponseWrapperDto response = trainerReviewService
                .getAllReviewByTrainerId(trainerId,pageNo,pageSize, sortBy,sortDirection);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/update")
    public ResponseEntity<ReviewResponseDto> updateReviewByReviewId(@RequestParam String reviewId,
                                                                    @Valid @RequestBody ReviewUpdateRequestDto requestDto)
    {
        log.info("Successfully received to update review for trainer {} by {} of review id: {}",
                requestDto.getTrainerId(),requestDto.getUserName(),reviewId);
        ReviewResponseDto response = trainerReviewService.updateReviewForTrainerById(reviewId,requestDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteReviewById(@RequestParam String reviewId, @RequestParam String trainerId) {
        log.info("Request received to delete review by reviewId: {} for trainer id: {}",reviewId,trainerId);
        String response = trainerReviewService.deleteReviewForTrainerByReviewId(reviewId,trainerId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
