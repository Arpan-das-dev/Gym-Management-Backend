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

/**
 * REST controller to manage trainer review operations such as adding,
 * retrieving, updating, and deleting reviews for trainers.
 *
 * <p>This controller maps HTTP requests targeting trainer reviews to the
 * underlying {@link TrainerReviewService} which performs business logic.</p>
 *
 * <p>Input validation is enforced using {@code @Validated} and Java Bean
 * Validation annotations on request parameters and bodies.</p>
 *
 * Base URL for this controller is defined by property: {@code "${trainer-service.Base_Url.Review}"}.
 *
 * @author Arpan Das
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("${trainer-service.Base_Url.Review}")
@RequiredArgsConstructor
@Validated
public class TrainerReviewController {

    /**
     * Service layer dependency for managing trainer review operations.
     */
    private final TrainerReviewService trainerReviewService;

    /**
     * Adds a new review for a trainer identified by trainerId with review data
     * provided in the request body.
     *
     * @param trainerId  the unique identifier of the trainer to review
     * @param requestDto the validated DTO containing review details
     * @return a {@link ResponseEntity} with status {@code 201 Created} and the created {@link ReviewResponseDto}
     */
    @PostMapping("/add")
    public ResponseEntity<ReviewResponseDto> addReviewForMember (@RequestParam String trainerId,
                                                                 @Valid @RequestBody ReviewAddRequestDto requestDto) {
        log.info("Request received to  add review for trainer {} by {} ",trainerId,requestDto.getUserName());
        ReviewResponseDto response = trainerReviewService.addReviewForMemberByUser(trainerId,requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all reviews for a given trainer with pagination and sorting options.
     *
     * @param trainerId     the unique identifier of the trainer
     * @param pageNo        the page number, must be zero or positive
     * @param pageSize      the size of the page, must be positive
     * @param sortBy        the field by which to sort the results
     * @param sortDirection the direction of sorting, e.g., ASC or DESC
     * @return a {@link ResponseEntity} with status {@code 200 OK} and a wrapped list of reviews {@link AllReviewResponseWrapperDto}
     */
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

    /**
     * Updates an existing review identified by reviewId with data from the
     * validated request DTO.
     *
     * @param reviewId   the unique identifier of the review to update
     * @param requestDto the updated review data
     * @return a {@link ResponseEntity} with status {@code 202 Accepted} and the updated {@link ReviewResponseDto}
     */
    @PutMapping("/update")
    public ResponseEntity<ReviewResponseDto> updateReviewByReviewId(@RequestParam String reviewId,
                                                                    @Valid @RequestBody ReviewUpdateRequestDto requestDto)
    {
        log.info("Successfully received to update review for trainer {} by {} of review id: {}",
                requestDto.getTrainerId(),requestDto.getUserName(),reviewId);
        ReviewResponseDto response = trainerReviewService.updateReviewForTrainerById(reviewId,requestDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * Deletes a review by its ID for a particular trainer.
     *
     * @param reviewId  the unique identifier of the review to delete
     * @param trainerId the unique identifier of the associated trainer
     * @return a {@link ResponseEntity} with status {@code 202 Accepted} and a deletion confirmation message
     */
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteReviewById(@RequestParam String reviewId, @RequestParam String trainerId) {
        log.info("Request received to delete review by reviewId: {} for trainer id: {}",reviewId,trainerId);
        String response = trainerReviewService.deleteReviewForTrainerByReviewId(reviewId,trainerId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
