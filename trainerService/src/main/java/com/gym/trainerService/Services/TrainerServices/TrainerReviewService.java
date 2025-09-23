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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerReviewService {

    private final TrainerRepository trainerRepository;
    private final ReviewRepository reviewRepository;

    @Transactional
    @Caching(
            put = {
                    @CachePut(value = "trainerCache", key = "#trainerId", unless = "#result == null")
            },
            evict = {
                    @CacheEvict(value = "allReviewCache", key = "#trainerId"),
                    @CacheEvict(value = "AllTrainerCache", key = "'All'")
            }
    )
    public ReviewResponseDto addReviewForMemberByUser(String trainerId, ReviewAddRequestDto requestDto) {
        Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new NoTrainerFoundException(
                        "No trainer found with the id: " + trainerId));
        log.info("Successfully retrieved trainer from db with id {}", trainer.getTrainerId());
        Review review = Review.builder()
                .userId(requestDto.getUserId()).userName(requestDto.getUserName())
                .userRole(requestDto.getUserRole())
                .reviewDate(requestDto.getReviewDate())
                .comment(requestDto.getComment())
                .review(requestDto.getReview())
                .trainer(trainer)
                .build();
       reviewRepository.save(review);
       Double averageReview = reviewRepository.getReviewsByTrainerId(trainerId);
       trainer.setAverageRating(averageReview);
       trainerRepository.save(trainer);
       log.info("Successfully saved a review with id {} on {}",review.getReviewId(), LocalDateTime.now());
       return reviewResponseBuilder(review);
    }

    @Cacheable(value = "allReviewCache",key = "#trainerId")
    public AllReviewResponseWrapperDto getAllReviewByTrainerId(String trainerId) {
        List<Review> reviews = reviewRepository.findReviewsByTrainerId(trainerId);
        log.info("Successfully fetched {} reviews from database for trainer id: {}", reviews.size(),trainerId);
        List<ReviewResponseDto> reviewResponseDtoList = reviews.stream().map(review-> ReviewResponseDto.builder()
                .reviewId(review.getReviewId())
                .userId(review.getUserId()).userName(review.getUserName())
                .userRole(review.getUserRole())
                .reviewDate(review.getReviewDate())
                .helpFullVote(review.getHelpFullVote()).notHelpFullVote(review.getNotHelpFullVote())
                .comment(review.getComment())
                .review(review.getReview())
                .build()).toList();
        log.info("Successfully building review list for {} no of reviews", reviewResponseDtoList.size());
        return AllReviewResponseWrapperDto.builder()
                .reviewResponseDtoList(reviewResponseDtoList)
                .build();
    }

    @Transactional
    @Caching(
            put = {
                    @CachePut(value = "trainerCache", key = "#requestDto.trainerId", unless = "#result == null")
            },
            evict = {
                    @CacheEvict(value = "allReviewCache", key = "#requestDto.trainerId"),
                    @CacheEvict(value = "AllTrainerCache", key = "'All'")
            }
    )
    public ReviewResponseDto updateReviewForTrainerById(String reviewId, @Valid ReviewUpdateRequestDto requestDto) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(()-> new NoReviewFoundException("No review find with this id : "+reviewId));
        Trainer trainer = trainerRepository.findById(review.getTrainer().getTrainerId())
                .orElseThrow(() -> new NoTrainerFoundException(
                        "No trainer found with the id: " + review.getTrainer().getTrainerId()));
        boolean condition1 = review.getUserId().equals(requestDto.getUserId()) &&
                review.getTrainer().getTrainerId().equals(requestDto.getTrainerId());
        boolean condition2 = review.getUserName().equals(requestDto.getUserName()) &&
                review.getUserRole().equals(requestDto.getUserRole());

        if(condition1 && condition2) {
            log.info("Fetched a valid review for trainer id {} by {} ",
                    review.getTrainer().getTrainerId(),review.getUserName());
            review.setReviewDate(requestDto.getReviewDate());
            review.setComment(requestDto.getComment());
            review.setReview(requestDto.getReview());
            reviewRepository.save(review);
            log.info("Successfully updated review for review id: {}",review.getReviewId());
            trainer.setAverageRating(reviewRepository.getReviewsByTrainerId(review.getTrainer().getTrainerId()));
            return reviewResponseBuilder(review);
        }
        throw new InvalidReviewException("Invalid review found doesn't matches user role or user name " +
                "or user id or trainer id ");
    }

    @Transactional
    @Caching(
            put = {
                    @CachePut(value = "trainerCache", key = "#trainerId", unless = "#result == null")
            },
            evict = {
                    @CacheEvict(value = "allReviewCache", key = "#trainerId"),
                    @CacheEvict(value = "AllTrainerCache", key = "'All'")
            }
    )
    public String deleteReviewForTrainerByReviewId(String reviewId,String trainerId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(()-> new NoReviewFoundException("No review find with this id : "+reviewId));
        reviewRepository.deleteById(review.getReviewId());
        Double averageRating = reviewRepository.getReviewsByTrainerId(review.getTrainer().getTrainerId());
        Trainer trainer = trainerRepository.findById(review.getTrainer().getTrainerId())
                .orElseThrow(() -> new NoTrainerFoundException(
                        "No trainer found with the id: " + review.getTrainer().getTrainerId()));
        if (averageRating.equals(trainer.getAverageRating())) {
            trainer.setAverageRating(averageRating);
            trainerRepository.save(trainer);
        }
        return "Successfully deleted review for review id: "+review.getReviewId()+ "for trainer id: "+trainerId;
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
