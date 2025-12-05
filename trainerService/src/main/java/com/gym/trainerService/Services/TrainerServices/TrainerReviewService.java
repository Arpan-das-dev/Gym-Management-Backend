package com.gym.trainerService.Services.TrainerServices;

import com.gym.trainerService.Dto.TrainerReviewDto.Requests.ReviewAddRequestDto;
import com.gym.trainerService.Dto.TrainerReviewDto.Requests.ReviewUpdateRequestDto;
import com.gym.trainerService.Dto.TrainerReviewDto.Responses.RatingMatrixInfo;
import com.gym.trainerService.Dto.TrainerReviewDto.Responses.ReviewResponseDto;
import com.gym.trainerService.Dto.TrainerReviewDto.Wrapper.AllReviewResponseWrapperDto;
import com.gym.trainerService.Exception.Custom.InvalidReviewException;
import com.gym.trainerService.Exception.Custom.NoReviewFoundException;
import com.gym.trainerService.Exception.Custom.NoTrainerFoundException;
import com.gym.trainerService.Models.Review;
import com.gym.trainerService.Models.Trainer;
import com.gym.trainerService.Repositories.ReviewRepository;
import com.gym.trainerService.Repositories.TrainerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
/**
 * Service class responsible for handling all business logic associated
 * with trainer reviews, including adding, updating, deleting, and
 * retrieving trainer reviews. It also ensures cache consistency and
 * transactional data safety during operations.
 *
 * <p>This class interacts with the underlying data layer through
 * {@link TrainerRepository} and {@link ReviewRepository} and manages
 * {@link Review} persistence and trainer average rating updates.</p>
 *
 * <p>Primarily used inside controller classes or service-layer orchestration
 * where review-related operations are required. Designed to encapsulate
 * review-handling logic to maintain code separation from controller logic.</p>
 *
 * @author Arpan Das
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerReviewService {

    /**
     * Repository interface for managing {@link Trainer} entities
     * and related operations.
     */
    private final TrainerRepository trainerRepository;

    /**
     * Repository interface for handling {@link Review} entity persistence
     * and query operations.
     */
    private final ReviewRepository reviewRepository;

    /**
     * Adds a new review for a specific trainer. This method retrieves the
     * trainer entity from the database, constructs a review instance using
     * builder pattern, and persists it. Afterward, it updates the average
     * trainer rating and evicts relevant caches to maintain data freshness.
     *
     * <p>The entire process is wrapped within a transaction to guarantee
     * atomicity. If an exception occurs, changes are rolled back.</p>
     *
     * @param trainerId  the unique trainer identifier
     * @param requestDto the DTO containing review request data
     * @return a structured {@link ReviewResponseDto} representing the saved review
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "AllTrainerCache", key = "'All'"),
            @CacheEvict(value = "trainerCache", key = "#trainerId"),
            @CacheEvict(value = "reviewCache", key = "#trainerId + '*' "),
            @CacheEvict(value = "ratingMatrix" , key = "#trainerId")
    })
    public ReviewResponseDto addReviewForMemberByUser(String trainerId, ReviewAddRequestDto requestDto) {
        // fetching the trainer by id from db (if valid) otherwise throws exception
        Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new NoTrainerFoundException(
                        "No trainer found with the id: " + trainerId));
        log.info("Successfully retrieved trainer from db with id {}", trainer.getTrainerId());
        // Building the review to save in the db by using builder pattern
        Review review = Review.builder()
                .userId(requestDto.getUserId())
                .userName(requestDto.getUserName())
                .userRole(requestDto.getUserRole())
                .reviewDate(requestDto.getReviewDate())
                .comment(requestDto.getComment())
                .review(requestDto.getReview())
                .trainer(trainer)
                .build();
        reviewRepository.save(review); // saving the review in the db
        Double averageReview = reviewRepository.getReviewsByTrainerId(trainerId); // getting the avg review
        if(trainer.getAverageRating() != averageReview) { // if the review is not equal only then change
            trainer.setAverageRating(averageReview);     // otherwise not (less db interaction)
            trainerRepository.save(trainer);
        }
        log.info("Successfully saved a review with id {} on {}", review.getReviewId(), LocalDateTime.now());
        return reviewResponseBuilder(review);    // returning the response dto using a helper method
    }

    /**
     * Retrieves all reviews for a particular trainer in a paginated and sorted manner.
     * The result is cached to enhance subsequent read performance.
     *
     * <p>Useful for displaying batch review data in paginated UI components
     * or analytics dashboards while maintaining consistent performance.</p>
     *
     * @param trainerId      the unique trainer identifier
     * @param pageNo         the page number to fetch
     * @param pageSize       the number of items per page
     * @param sortBy         the field used for sorting
     * @param sortDirection  the direction of sorting, either {@code ASC} or {@code DESC}
     * @return an {@link AllReviewResponseWrapperDto} containing a list of review DTOs
     */
    @Cacheable(
            value = "reviewCache",
            key = "#trainerId + ':' + #pageNo + ':' + #pageSize + ':' + #sortBy + ':' + #sortDirection"
    )
    public AllReviewResponseWrapperDto getAllReviewByTrainerId(String trainerId, int pageNo, int pageSize,
                                                               String sortBy, String sortDirection) {
        // building sort by using sortingBy and sorting direction(ascending/descending)
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        // building the page request by page size, page no and sort
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        log.info("request reached in service logic to get reviews by pageNo {} & pageSize {} ", pageNo,pageSize);
        // retrieving reviews from database in list dataType
        List<Review> reviews = reviewRepository.findReviewsByTrainerId(trainerId, pageable).stream().toList();
        log.info("Successfully fetched {} reviews from database for trainer id: {}", reviews.size(), trainerId);
        // using stream and map to make reviewResponseDtoList to return it through AllReviewResponseWrapperDto
        List<ReviewResponseDto> reviewResponseDtoList = reviews.stream()
                .map(review -> ReviewResponseDto.builder()
                        .reviewId(review.getReviewId())
                        .userId(review.getUserId()).userName(review.getUserName())
                        .userRole(review.getUserRole())
                        .reviewDate(review.getReviewDate())
                        .helpFullVote(review.getHelpFullVote())
                        .notHelpFullVote(review.getNotHelpFullVote())
                        .comment(review.getComment())
                        .review(review.getReview())
                        .build())
                .toList();
        log.info("Successfully building review list for {} no of reviews", reviewResponseDtoList.size());
        // returning the review
        return AllReviewResponseWrapperDto.builder()
                .reviewResponseDtoList(reviewResponseDtoList)
                .build();
    }

    /**
     * Updates an existing trainer review based on its review ID and corresponding
     * request data. Ensures that user identity and trainer integrity match before
     * applying updates.
     *
     * <p>Cache eviction ensures that any outdated review data is purged, and
     * transactional context guarantees data consistency if any exception occurs.</p>
     *
     * @param reviewId   the unique identifier of the review to update
     * @param requestDto contains updated review data
     * @return the updated {@link ReviewResponseDto}
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "trainerCache", key = "#requestDto.trainerId"),
            @CacheEvict(value = "AllTrainerCache", key = "'All'"),
            @CacheEvict(value = "reviewCache", key = "#requestDto.trainerId + '*'"),
            @CacheEvict(value = "ratingMatrix" , key = "#requestDto.trainerId")
    })
    public ReviewResponseDto updateReviewForTrainerById(String reviewId, ReviewUpdateRequestDto requestDto) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NoReviewFoundException(
                        "No review find with this id : " + reviewId));
        Trainer trainer = trainerRepository.findById(review.getTrainer().getTrainerId())
                .orElseThrow(() -> new NoTrainerFoundException(
                        "No trainer found with the id: " + review.getTrainer().getTrainerId()));
        boolean condition1 = review.getUserId().equals(requestDto.getUserId()) &&
                review.getTrainer().getTrainerId().equals(requestDto.getTrainerId());
        boolean condition2 = review.getUserName().equals(requestDto.getUserName()) &&
                review.getUserRole().equals(requestDto.getUserRole());

        if (condition1 && condition2) {
            log.info("Fetched a valid review for trainer id {} by {} ",
                    review.getTrainer().getTrainerId(), review.getUserName());
            review.setReviewDate(requestDto.getReviewDate());
            review.setComment(requestDto.getComment());
            review.setReview(requestDto.getReview());
            reviewRepository.save(review);
            log.info("Successfully updated review for review id: {}", review.getReviewId());
            Double averageReview = reviewRepository
                    .getReviewsByTrainerId(review.getTrainer().getTrainerId());// getting the avg review
            if(trainer.getAverageRating() != averageReview) { // if the review is not equal only then change
                trainer.setAverageRating(averageReview);     // otherwise not (less db interaction)
                trainerRepository.save(trainer);
            }

            return reviewResponseBuilder(review);
        }
        throw new InvalidReviewException("Invalid review found doesn't matches user role or user name " +
                "or user id or trainer id ");
    }

    /**
     * Deletes a review identified by its ID for a given trainer.
     * After a successful delete, the trainer’s average rating is recalculated.
     *
     * <p>Transactional context ensures proper rollback in case of failure,
     * and associated caches are cleared upon completion.</p>
     *
     * @param reviewId  the identifier of the review to remove
     * @param trainerId the trainer associated with the review
     * @return confirmation message indicating successful deletion
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "trainerCache", key = "#trainerId"),
            @CacheEvict(value = "AllTrainerCache", key = "'All'"),
            @CacheEvict(value = "reviewCache", key = "#trainerId + '*'"),
            @CacheEvict(value = "ratingMatrix" , key = "#trainerId")
    })
    public String deleteReviewForTrainerByReviewId(String reviewId, String trainerId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NoReviewFoundException(
                        "No review find with this id : " + reviewId));
        reviewRepository.deleteById(review.getReviewId());
        Double averageRating = reviewRepository.getReviewsByTrainerId(review.getTrainer().getTrainerId());
        Trainer trainer = trainerRepository.findById(review.getTrainer().getTrainerId())
                .orElseThrow(() -> new NoTrainerFoundException(
                        "No trainer found with the id: " + review.getTrainer().getTrainerId()));
        if (averageRating.equals(trainer.getAverageRating())) {
            trainer.setAverageRating(averageRating);
            trainerRepository.save(trainer);
        }
        return "Successfully deleted review for review id: " + review.getReviewId() + "for trainer id: "
                + trainerId;
    }

    /**
     * Helper method that constructs a {@link ReviewResponseDto} instance
     * from a given {@link Review} entity.
     *
     * @param review the review entity from which response data is built
     * @return a fully populated {@link ReviewResponseDto}
     */
    private ReviewResponseDto reviewResponseBuilder(Review review) {
        return ReviewResponseDto.builder()
                .reviewId(review.getReviewId())
                .userId(review.getUserId())
                .userName(review.getUserName())
                .userRole(review.getUserRole())
                .reviewDate(review.getReviewDate())
                .review(review.getReview())
                .helpFullVote(review.getHelpFullVote())
                .notHelpFullVote(review.getNotHelpFullVote())
                .comment(review.getComment())
                .build();
    }

    @Cacheable(value = "ratingMatrix" , key = "#trainerId")
    public RatingMatrixInfo getRatingMatrix(String trainerId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime firstDayOfCurrentMonth = now.withDayOfMonth(1);

        log.info("⭐⭐⭐ get request to get review matrix for trainer {} from {} to {} range",
                trainerId, firstDayOfCurrentMonth, now);

        Double currentRatingRaw = reviewRepository.getReviewsByTrainerId(trainerId);
        Double oldRatingRaw = reviewRepository.getReviewBYTrainerIdWithDate(trainerId, firstDayOfCurrentMonth);

        // 1. Handle NULL results by converting them to 0.00
        double currentRating = (currentRatingRaw != null) ? currentRatingRaw : 0.00;
        double oldRating = (oldRatingRaw != null) ? oldRatingRaw:0.00 ;

        double change = currentRating - oldRating;

        // 2. Handle Division by Zero for Percentage Calculation
        double percentageChange = 0.00;
        if (currentRating != 0.00) {
            percentageChange = (change / currentRating) * 100;
        }

        log.info("⭐⭐⭐ current rating is {} where the older one is {} and the change in % is {}%",
                currentRating, oldRating, percentageChange);

        return RatingMatrixInfo.builder()
                .currentRating(currentRating)
                .oldRating(oldRating)
                .change(percentageChange)
                .build();
    }
}
