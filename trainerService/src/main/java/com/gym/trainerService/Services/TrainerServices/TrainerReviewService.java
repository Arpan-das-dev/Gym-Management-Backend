package com.gym.trainerService.Services.TrainerServices;

import com.gym.trainerService.Dto.TrainerReviewDto.Requests.ReviewAddRequestDto;
import com.gym.trainerService.Dto.TrainerReviewDto.Requests.ReviewUpdateRequestDto;
import com.gym.trainerService.Dto.TrainerReviewDto.Responses.RatingMatrixInfo;
import com.gym.trainerService.Dto.TrainerReviewDto.Responses.ReviewResponseDto;
import com.gym.trainerService.Dto.TrainerReviewDto.Wrapper.AllReviewResponseWrapperDto;
import com.gym.trainerService.Exception.Custom.InvalidReviewException;
import com.gym.trainerService.Exception.Custom.NoReviewFoundException;
import com.gym.trainerService.Exception.Custom.NoTrainerFoundException;
import com.gym.trainerService.Exception.Custom.UnAuthorizedRequestException;
import com.gym.trainerService.Models.Review;
import com.gym.trainerService.Models.Trainer;
import com.gym.trainerService.Repositories.ReviewRepository;
import com.gym.trainerService.Repositories.TrainerRepository;
import com.gym.trainerService.Utils.CustomAnnotations.Annotations.LogRequestTime;
import com.gym.trainerService.Utils.CustomCacheEvict;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

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
    private final CacheManager cacheManager;
    private final CustomCacheEvict evict;

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
    @LogRequestTime
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "AllTrainerCache", key = "'All'"),
            @CacheEvict(value = "trainerCache", key = "#trainerId"),
            @CacheEvict(value = "reviewCache", key = "#requestDto.userId"),
            @CacheEvict(value = "DashboardInfo",key = "#trainerId"),
            @CacheEvict(value = "ratingMatrix" , key = "#trainerId")
    })
    public String  addReviewForTrainerByUser(String trainerId, ReviewAddRequestDto requestDto) {
        // fetching the trainer by id from db (if valid) otherwise throws exception
        Trainer trainer = getById(trainerId);
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
        Double avg = reviewRepository.getReviewsByTrainerId(trainerId);
        double newAvg = (avg != null) ? avg : 0.0;
        trainerRepository.updateAverageRating(trainerId, newAvg);
        evict.evictReviewCacheByTrainerId(trainerId);
        log.info("Successfully saved a review for trainer {} at {}", trainerId, LocalDateTime.now());
        return String.format(
                "Thanks for your feedback! Your %.1f-star review has been added successfully.",
                review.getReview()
        );
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
     * @param sortDirection  the direction of sorting, either {@code ASC} or {@code DESC}
     * @return an {@link AllReviewResponseWrapperDto} containing a list of review DTOs
     */
    @LogRequestTime
    @Cacheable(
            value = "reviewCache",
            key = "#trainerId + ':' + #pageNo + ':' + #pageSize +  ':' + #sortDirection"
    )
    public AllReviewResponseWrapperDto getAllReviewByTrainerId(String trainerId, int pageNo, int pageSize,
                                                               String sortDirection) {
        // building sort by using sortingBy and sorting direction(ascending/descending)
        Trainer trainer = getById(trainerId);
        log.info("®️®️ request received to get review for trainer {} {} ",
                trainer.getFirstName(),trainer.getLastName());
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction,"reviewDate");
        // building the page request by page size, page no and sort
        Pageable pageable = PageRequest.of(pageNo, pageSize,sort);
        log.info("request reached in service logic to get reviews by pageNo {} & pageSize {} ", pageNo,pageSize);

        // retrieving reviews from database in list dataType
        Page<Review> reviews = reviewRepository.findReviewsByTrainerId(trainerId, pageable);
        log.info("Successfully fetched {} reviews from database for trainer id: {}", reviews.getSize(), trainerId);
        // using stream and map to make reviewResponseDtoList to return it through AllReviewResponseWrapperDto
        List<ReviewResponseDto> reviewResponseDtoList = reviews.stream()
                .map(review -> ReviewResponseDto.builder()
                        .reviewId(review.getReviewId())
                        .userId(review.getUserId()).userName(review.getUserName())
                        .userRole(review.getUserRole())
                        .reviewDate(review.getReviewDate())
                        .comment(review.getComment())
                        .review(review.getReview())
                        .build())
                .toList();
        log.info("Successfully building review list for {} no of reviews", reviewResponseDtoList.size());
        // returning the review
        return AllReviewResponseWrapperDto.builder()
                .reviewResponseDtoList(reviewResponseDtoList)
                .pageNo(reviews.getNumber())
                .pageSize(reviews.getSize())
                .totalElements(reviews.getTotalElements())
                .totalPages(reviews.getTotalPages())
                .lastPage(reviews.isLast())
                .build();
    }

    @LogRequestTime
    @Cacheable(value = "reviewCache", key = "#userId")
    public AllReviewResponseWrapperDto getReviewByUserId(String userId,int pageNo,int pageSize, String sortDirection) {
        log.info("👔👔 request receive to get reviews for -->{}",userId);
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable page = PageRequest.of(pageNo,pageSize,Sort.by(direction,"reviewDate"));
        Page<Review> reviews = reviewRepository.findByUserId(userId,page);
        List<ReviewResponseDto> reviewResponseDtoList = reviews.stream()
                .map(review -> ReviewResponseDto.builder()
                        .reviewId(review.getReviewId())
                        .userId(review.getUserId()).userName(review.getUserName())
                        .userRole(review.getUserRole())
                        .reviewDate(review.getReviewDate())
                        .comment(review.getComment())
                        .review(review.getReview())
                        .build())
                .toList();
        log.info("Successfully building review list for {} no of reviews for user ", reviewResponseDtoList.size());
        // returning the review
        return AllReviewResponseWrapperDto.builder()
                .reviewResponseDtoList(reviewResponseDtoList)
                .pageNo(reviews.getNumber())
                .pageSize(reviews.getSize())
                .totalElements(reviews.getTotalElements())
                .totalPages(reviews.getTotalPages())
                .lastPage(reviews.isLast())
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
    @LogRequestTime
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "trainerCache", key = "#requestDto.trainerId"),
            @CacheEvict(value = "AllTrainerCache", key = "'All'"),
            @CacheEvict(value = "reviewCache", key = "#requestDto.userId"),
            @CacheEvict(value = "ratingMatrix" , key = "#requestDto.trainerId"),
            @CacheEvict(value = "DashboardInfo",key = "#requestDto.trainerId")
    })
    public ReviewResponseDto updateReviewForTrainerById(String reviewId, ReviewUpdateRequestDto requestDto) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NoReviewFoundException("No review found"));

        if (!review.getUserId().equals(requestDto.getUserId()) ||
                !review.getTrainer().getTrainerId().equals(requestDto.getTrainerId()) ||
                !review.getUserName().equals(requestDto.getUserName()) ||
                !review.getUserRole().equals(requestDto.getUserRole()))
        {
            throw new InvalidReviewException("Invalid review credentials.");
        }

        String tId = review.getTrainer().getTrainerId();

        review.setReviewDate(requestDto.getReviewDate());
        review.setComment(requestDto.getComment());
        review.setReview(requestDto.getReview());
        reviewRepository.save(review);

        Double avg = reviewRepository.getReviewsByTrainerId(tId);
        double newAvg = (avg != null) ? avg : 0.0;

        trainerRepository.updateAverageRating(tId, newAvg);

        evict.evictReviewCacheByTrainerId(tId);

        log.info("Successfully updated review {}", reviewId);

        return reviewResponseBuilder(review);
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
    @LogRequestTime
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "trainerCache", key = "#trainerId"),
            @CacheEvict(value = "AllTrainerCache", key = "'All'"),
            @CacheEvict(value = "reviewCache", key = "#userId"),
            @CacheEvict(value = "ratingMatrix", key = "#trainerId"),
            @CacheEvict(value = "DashboardInfo", key = "#trainerId")
    })
    public String deleteReviewForTrainerByReviewId(String reviewId, String trainerId, String userId) {

        Trainer trainer = getById(trainerId);
        // 1) load & validate
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NoReviewFoundException("No review found"));
        if (!review.getUserId().equals(userId)) {
            throw new UnAuthorizedRequestException("You can only delete your own reviews");
        }
        if (!review.getTrainer().getTrainerId().equals(trainerId)) {
            throw new NoTrainerFoundException("Trainer id mismatch in review");
        }
        String tId = review.getTrainer().getTrainerId();

        reviewRepository.deleteById(reviewId);
        reviewRepository.flush();
        Double avg = reviewRepository.getReviewsByTrainerId(tId); // avg or null
        double newAvg = (avg != null) ? avg : 0.0;
        int effectedRows =  trainerRepository.updateAverageRating(tId, newAvg);
        log.info("Deleted successfully and rows effected ==> {}",effectedRows);
        evict.evictReviewCacheByTrainerId(tId);

        return "Successfully deleted review for "+trainer.getFirstName()+" "+trainer.getLastName();
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
                .comment(review.getComment())
                .build();
    }

    @LogRequestTime
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

    private Trainer getById(String trainerId){
        Trainer trainer = null;

        Cache.ValueWrapper wrapper = Objects.requireNonNull(cacheManager.getCache("trainer")).get(trainerId);

        if (wrapper != null) {
            try {
                trainer = (Trainer) wrapper.get();
                log.info("Trainer {} {} found in cache", Objects.requireNonNull(trainer).getFirstName(), trainer.getLastName());
            } catch (ClassCastException e) {
                log.warn("Error occurred during cache cast for trainerId {}: {}", trainerId, e.getLocalizedMessage());
            }
        }

        if (trainer == null) {
            trainer = trainerRepository.findById(trainerId)
                    .orElseThrow(()-> new NoTrainerFoundException("No Trainer Found with this id"));

            log.info("Reloaded trainer {} {} from db", trainer.getFirstName(), trainer.getLastName());
            Objects.requireNonNull(cacheManager.getCache("trainer")).put(trainer.getTrainerId(), trainer);
        }

        log.info("review service returning {} {} by private method", trainer.getFirstName(), trainer.getLastName());
        return trainer;
    }
}
