package com.gym.trainerService.Controllers;

import com.gym.trainerService.Services.TrainerServices.TrainerProfileService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for managing trainer profile image operations
 * such as uploading, retrieving, and deleting profile images.
 *
 * <p>This controller maps HTTP requests related to trainer profile images
 * to the underlying {@link TrainerProfileService} to handle business logic.</p>
 *
 * <p>All endpoints log requests for traceability and return
 * {@link ResponseEntity} wrapping status codes and messages appropriate to each operation.</p>
 *
 * Base URL for this controller is defined by property: {@code "${trainer-service.Base_Url.Profile}"}.
 *
 * @author Arpan Das
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("${trainer-service.Base_Url.Profile}")
@RequiredArgsConstructor
public class TrainerProfileController {

    /**
     * Service layer dependency to manage profile image operations.
     */
    private final TrainerProfileService profileService;

    /**
     * Uploads a profile image for a specific trainer.
     *
     * <p>The image is uploaded as a multipart form data request parameter.
     * The trainer ID is required and must not be blank.</p>
     *
     * @param trainerId the unique identifier of the trainer whose image is being uploaded
     * @param image     the image file to upload
     * @return a {@link ResponseEntity} with HTTP status {@code 201 Created} and an upload response message
     */

    @PostMapping("/trainer/upload")
    public ResponseEntity<String> uploadTrainerImage(@RequestParam @NotBlank String trainerId,
                                                     @RequestParam("image") MultipartFile image) {
        log.info("Request received to upload image for the trainer :: {}",trainerId);
        String response = profileService.uploadImage(trainerId,image);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves the URL of the profile image for the specified trainer.
     *
     * <p>Returns the accessible URL string to the stored profile image.</p>
     *
     * @param trainerId the unique identifier of the trainer whose profile image URL is requested
     * @return a {@link ResponseEntity} with HTTP status {@code 200 OK} and the profile image URL as the body
     */
    @GetMapping("/all/image")
    public ResponseEntity<String> getProfileImage(@RequestParam @NotBlank String trainerId) {
        log.info("Request received to get profile image url for trainer :: {}",trainerId);
        String response = profileService.getProfileImageUrl(trainerId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Deletes the profile image associated with a specific trainer.
     *
     * <p>Deletes the stored image resource linked to the trainer. Returns a
     * confirmation message upon successful deletion.</p>
     *
     * @param trainerId the unique identifier of the trainer whose profile image is to be deleted
     * @return a {@link ResponseEntity} with HTTP status {@code 202 Accepted} and deletion confirmation message
     */
    @DeleteMapping("/trainer/delete")
    public ResponseEntity<String> deleteTrainerImage(@RequestParam @NotBlank String trainerId) {
        log.info("Request received to delete profile image url for trainer :: {}",trainerId);
        String response = profileService.deleteProfileImageUrl(trainerId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
