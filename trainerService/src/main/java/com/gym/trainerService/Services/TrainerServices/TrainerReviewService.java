package com.gym.trainerService.Services.TrainerServices;

import com.gym.trainerService.Dto.TrainerReviewDto.Requests.ReviewAddRequestDto;
import com.gym.trainerService.Dto.TrainerReviewDto.Requests.ReviewUpdateRequestDto;
import com.gym.trainerService.Dto.TrainerReviewDto.Responses.ReviewResponseDto;
import com.gym.trainerService.Dto.TrainerReviewDto.Wrapper.AllReviewResponseWrapperDto;
import com.gym.trainerService.Exception.InvalidReviewException;
import com.gym.trainerService.Exception.NoReviewFoundException;
import com.gym.trainerService.Exception.NoTrainerFoundException;
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
 * service class for trainer 
 * {@code Author:}  Arpan Das
 * {@code Version :}  1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerReviewService {

    // injecting trainer & review repository dependencies by constructor (@RequestParam)
    private final TrainerRepository trainerRepository;
    private final ReviewRepository reviewRepository;

    /**
     * this method is responsible to add a new review for a trainer
     * with trainer id from a request dto
     * after saving the review it also sets the
     * average rating of the trainer if not equals
     * and at the same time evicting the cache to keep things
     * updated and fresh
     * transactional is used to make sure that
     * if anything goes wrong data will be rolled back
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "AllTrainerCache", key = "'All'"),
            @CacheEvict(value = "trainerCache", key = "#trainerId"),
            @CacheEvict(value = "reviewCache", key = "#trainerId + '*' ")
    })
    public ReviewResponseDto addReviewForMemberByUser(String trainerId, ReviewAddRequestDto requestDto) {
        // fetching the trainer by id from db (if valid) otherwise throws exception
        Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new NoTrainerFoundException(
                        "No trainer found with the id: " + trainerId));
        log.info("Successfully retrieved trainer from db with id {}", trainer.getTrainerId());
        // Building the review to save in the db by using builder pattern
        Review review = Review.builder()
                .userId(requestDto.getUserId()).userName(requestDto.getUserName())
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
     * this method is responsible to retrieve reviews
     * for a particular trainer
     * this method take trainerId , pageSize, pageNo, sortBy and sortDirection
     * to return reviews in small chunks as a wrapper of list of ReviewResponseDto
     * after successful retrieving data from database it saves the data
     * in cache with a key value of combination of all parameters
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
     * updating review id
     * it takes review id and request dto as parameter
     * Transactional is used to make sure that
     * if anything goes wrong data wil be rolled back
     * after successfully update it evict the cache
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "trainerCache", key = "#trainerId"),
            @CacheEvict(value = "AllTrainerCache", key = "'All'"),
            @CacheEvict(value = "reviewCache", key = "#trainerId + '*'")
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

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "trainerCache", key = "#trainerId"),
            @CacheEvict(value = "AllTrainerCache", key = "'All'"),
            @CacheEvict(value = "reviewCache", key = "#trainerId + '*'")
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

    private ReviewResponseDto reviewResponseBuilder(Review review) {
        return ReviewResponseDto.builder()
                .reviewId(review.getReviewId())
                .userId(review.getUserId()).userName(review.getUserName())
                .userRole(review.getUserRole())
                .reviewDate(review.getReviewDate()).review(review.getReview())
                .helpFullVote(review.getHelpFullVote()).notHelpFullVote(review.getNotHelpFullVote())
                .comment(review.getComment())
                .build();
    }

}
