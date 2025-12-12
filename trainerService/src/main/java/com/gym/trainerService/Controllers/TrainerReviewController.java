package com.gym.trainerService.Controllers;

import com.gym.trainerService.Dto.MemberDtos.Responses.GenericResponse;
import com.gym.trainerService.Dto.TrainerReviewDto.Requests.ReviewAddRequestDto;
import com.gym.trainerService.Dto.TrainerReviewDto.Requests.ReviewUpdateRequestDto;
import com.gym.trainerService.Dto.TrainerReviewDto.Responses.ReviewResponseDto;
import com.gym.trainerService.Dto.TrainerReviewDto.Wrapper.AllReviewResponseWrapperDto;
import com.gym.trainerService.Services.TrainerServices.TrainerReviewService;
import com.gym.trainerService.Utils.CustomAnnotations.Annotations.LogExecutionTime;
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
     * @return a {@link ResponseEntity} with status {@code 201 Created} and the created {@link GenericResponse}
     */
    @PostMapping("/user/add")
    public ResponseEntity<GenericResponse> addReviewForTrainer (@RequestParam String trainerId,
                                                                @Valid @RequestBody ReviewAddRequestDto requestDto) {
        log.info("©️©️ Request received to  add review for trainer {} by {} ",trainerId,requestDto.getUserName());
        String  response = trainerReviewService.addReviewForTrainerByUser(trainerId,requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new GenericResponse(response));
    }

    /**
     * Retrieves all reviews for a given trainer with pagination and sorting options.
     *
     * @param trainerId     the unique identifier of the trainer
     * @param pageNo        the page number, must be zero or positive
     * @param pageSize      the size of the page, must be positive
     * @param sortDirection the direction of sorting, e.g., ASC or DESC
     * @return a {@link ResponseEntity} with status {@code 200 OK} and a wrapped list of reviews {@link AllReviewResponseWrapperDto}
     */
    @GetMapping("/all/getAll")
    public ResponseEntity<AllReviewResponseWrapperDto> getAllReviewByTrainerId(
            @RequestParam @NotBlank(message = "trainerId must not be blank") String trainerId,
            @RequestParam @PositiveOrZero(message = "pageNo must be zero or positive") int pageNo,
            @RequestParam @Positive(message = "pageSize must be greater than zero") int pageSize,
            @RequestParam @NotBlank(message = "sortDirection must not be blank") String sortDirection) {
        log.info("©️©️ Request received to get all review for trainer id: {}", trainerId);
        AllReviewResponseWrapperDto response = trainerReviewService
                .getAllReviewByTrainerId(trainerId, pageNo, pageSize,  sortDirection);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * a method to get review for a particular user's id
     *
     * @param userId a unique identifier for user id
     * @param pageNo the page no which must be positive or zero
     * @param pageSize the size of the page, must be positive
     * @param sortDirection the direction of sorting, e.g., ASC or DESC
     * @return a {@link ResponseEntity} with status {@code 200 OK} and
     * a wrapped list of reviews {@link AllReviewResponseWrapperDto}
     * @see TrainerReviewService#getReviewByUserId(String, int, int, String)
     */
    @LogExecutionTime
    @GetMapping("/user/getReview")
    public ResponseEntity<AllReviewResponseWrapperDto> getReviewByUserId(
            @RequestParam @NotBlank(message = "userId must not be blank") String userId,
            @RequestParam @PositiveOrZero(message = "pageNo must be zero or positive") int pageNo,
            @RequestParam @Positive(message = "pageSize must be greater than zero") int pageSize,
            @RequestParam @NotBlank(message = "sortDirection must not be blank") String sortDirection
    ){
        log.info("©️©️ request received to get reviews for {} of page no -> [{}] for size -> [{}]",
                userId,pageNo,pageSize);
        AllReviewResponseWrapperDto responseWrapperDto = trainerReviewService
                .getReviewByUserId(userId,pageNo,pageSize,sortDirection);
        return ResponseEntity.status(HttpStatus.OK).body(responseWrapperDto);
    }

    /**
     * Updates an existing review identified by reviewId with data from the
     * validated request DTO.
     *
     * @param reviewId   the unique identifier of the review to update
     * @param requestDto the updated review data
     * @return a {@link ResponseEntity} with status {@code 202 Accepted} and the updated {@link ReviewResponseDto}
     */
    @PutMapping("/user/update")
    public ResponseEntity<GenericResponse> updateReviewByReviewId(@RequestParam String reviewId,
                                                                    @Valid @RequestBody ReviewUpdateRequestDto requestDto)
    {
        log.info("©️©️ Request received to update review for trainer {} by {} of review id: {}",
                requestDto.getTrainerId(),requestDto.getUserName(),reviewId);
        ReviewResponseDto response = trainerReviewService.updateReviewForTrainerById(reviewId,requestDto);
        log.info("✅✅ successfully saved the updated request  by {}",response.getUserName());
        String res = response.getUserName() + " We Have Successfully Updated Your Review";
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new GenericResponse(res));
    }

    /**
     * Deletes a review by its ID for a particular trainer.
     *
     * @param reviewId  the unique identifier of the review to delete
     * @param trainerId the unique identifier of the associated trainer
     * @return a {@link ResponseEntity} with status {@code 202 Accepted} and a deletion confirmation message
     */
    @DeleteMapping("/user/delete")
    public ResponseEntity<GenericResponse> deleteReviewById(
            @RequestParam String reviewId,
            @RequestParam String trainerId,
            @RequestParam String userId) {
        log.info("©️©️ Request received to delete review by reviewId: {} for trainer id: {}",reviewId,trainerId);
        String response = trainerReviewService.deleteReviewForTrainerByReviewId(reviewId,trainerId,userId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new GenericResponse(response));
    }
}
