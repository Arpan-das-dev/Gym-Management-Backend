package com.gym.trainerService.Services.TrainerServices;

import com.gym.trainerService.Exception.InvalidImageUrlException;
import com.gym.trainerService.Exception.NoTrainerFoundException;
import com.gym.trainerService.Models.Trainer;
import com.gym.trainerService.Repositories.TrainerRepository;
import com.gym.trainerService.Services.OtherServices.AwsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service layer for managing trainer profile images in the system.
 * <p>
 * This service integrates AWS S3 operations (via {@link AwsService}) with database persistence
 * and Spring Cache to provide high performance profile image management.
 * It allows uploading, retrieving, and deleting profile images for trainers.
 * </p>
 *
 * <p><b>Key Features:</b></p>
 * <ul>
 *   <li>Uploads trainer images to AWS S3 and saves URLs in the database</li>
 *   <li>Caches trainer profile URLs using Spring Cache for faster access</li>
 *   <li>Evicts or updates cache entries on upload and deletion events</li>
 * </ul>
 *
 * <p><b>Cache Details:</b></p>
 * <ul>
 *   <li><code>profileImageCache</code> — keyed by trainerId</li>
 *   <li>Uses <code>@Cacheable</code> for read, <code>@CachePut</code> for updates, and <code>@CacheEvict</code> for deletes</li>
 * </ul>
 *
 * <p><b>Best Practices:</b></p>
 * <ul>
 *   <li>Validate image size and content type before upload (for production security).</li>
 *   <li>Use async uploads for large files to prevent blocking I/O.</li>
 *   <li>Ensure cache TTL is aligned with S3 object lifecycle rules.</li>
 * </ul>
 *
 * @author Arpan
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerProfileService {
    /** AWS service for S3 upload and delete operations. */
    private final AwsService awsService;

    /** Repository for trainer entity persistence. */
    private final TrainerRepository trainerRepository;

    /**
     * Uploads a trainer's profile image to AWS S3 and updates the trainer record.
     * <p>
     * The uploaded image is stored in S3 using the {@link AwsService}, and the resulting URL
     * is persisted in the database and cached under the key {@code trainerId}.
     * </p>
     *
     * @param trainerId ID of the trainer whose profile image is being uploaded
     * @param image     the image file to upload
     * @return the public S3 URL of the uploaded image
     * @throws NoTrainerFoundException if no trainer exists with the given ID
     */
    @CachePut(value = "profileImageCache", key = "#trainerId")
    public String uploadImage(String trainerId, MultipartFile image) {
        Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new NoTrainerFoundException("No trainer found with the id: " + trainerId));
        log.info("Successfully retrieved trainer from db with id {}", trainer.getTrainerId());
        String url = awsService.uploadImage(trainerId,image);
        trainer.setTrainerProfileImageUrl(url);
        trainerRepository.save(trainer);
        log.info("Successfully uploaded trainer profile image url of id {}",trainer.getTrainerId());
        return url;
    }

    /**
     * Retrieves a trainer's profile image URL from cache or database.
     * <p>
     * If the data exists in cache, it avoids hitting the database.
     * Otherwise, it fetches from DB and populates the cache automatically.
     * </p>
     *
     * @param trainerId ID of the trainer
     * @return the profile image URL
     * @throws NoTrainerFoundException if no trainer exists with the given ID
     */
    @Cacheable(value = "profileImageCache", key = "#trainerId")
    public String getProfileImageUrl(String trainerId) {
        Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new NoTrainerFoundException("No trainer found with the id: " + trainerId));
        return trainer.getTrainerProfileImageUrl();
    }

    /**
     * Deletes a trainer's profile image from both AWS S3 and the database.
     * <p>
     * The corresponding cache entry is evicted to ensure data consistency.
     * </p>
     *
     * @param trainerId ID of the trainer whose image is to be deleted
     * @return a confirmation message indicating success
     * @throws NoTrainerFoundException   if the trainer does not exist
     * @throws InvalidImageUrlException  if the stored image URL is invalid or empty
     */
    @CacheEvict(value = "profileImageCache", key = "#trainerId")
    public String deleteProfileImageUrl(String trainerId) {
        Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new NoTrainerFoundException("No trainer found with the id: " + trainerId));
        log.info("Request processing for deleting image of trainer {} ",trainer.getTrainerId());
        if(trainer.getTrainerProfileImageUrl().isEmpty()){
            throw new InvalidImageUrlException("Invalid image URL");        // throw error if no image url found
        }
        awsService.deleteImage(trainer.getTrainerProfileImageUrl());
        trainer.setTrainerProfileImageUrl("");
        trainerRepository.save(trainer);
        return "Image deleted Successfully";
    }
}
