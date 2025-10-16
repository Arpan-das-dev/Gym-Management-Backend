package com.gym.trainerService.Controllers;

import com.gym.trainerService.Services.TrainerServices.TrainerStatusService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller responsible for managing trainer availability statuses.
 * <p>
 * This REST controller exposes endpoints to create, retrieve, and delete
 * the status of trainers. It interacts with the {@link TrainerStatusService}
 * to perform the business logic and returns standardized HTTP responses.
 * </p>
 *
 * <p><b>Base URL:</b> Configured through <code>${trainer-service.Base_Url.status}</code></p>
 *
 * <p>Example usage:</p>
 * <ul>
 *   <li>POST <code>/trainer/status</code> - to set a trainer's current status</li>
 *   <li>GET <code>/all/status</code> - to fetch a trainer's current status</li>
 *   <li>DELETE <code>/trainer/deleteStatus</code> - to remove a trainer's status record</li>
 * </ul>
 *
 * @author Arpan
 * @since 1.0
 */
@RestController
@Slf4j
@RequestMapping("${trainer-service.Base_Url.status}")
@RequiredArgsConstructor
public class TrainerStatusController {

    private final TrainerStatusService trainerStatusService;

    /**
     * Sets the availability status for a specific trainer.
     * <p>
     * Accepts parameters for the trainer's ID and the status to be applied.
     * The supported values depend on the application configuration (e.g., "AVAILABLE" or "UNAVAILABLE").
     * </p>
     *
     * @param status    the desired status to assign to the trainer; defaults to "UNAVAILABLE" if not provided
     * @param trainerId the unique identifier of the trainer
     * @return a {@link ResponseEntity} containing a success message and HTTP 201 (Created) status
     *
     * @see TrainerStatusService#setStatus(String, String)
     */
    @PostMapping("/trainer/status")
    public ResponseEntity<String> setStatusForTrainer(
            @RequestParam(defaultValue = "UNAVAILABLE") @NotBlank String status,
            @RequestParam @NotBlank String trainerId) {
        log.info("Request received to set status as :: {} for trainer :: {}", status, trainerId);
        String response = trainerStatusService.setStatus(status, trainerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves the current status of a specific trainer.
     * <p>
     * This endpoint fetches the trainer's availability or working status
     * previously set using {@link #setStatusForTrainer(String, String)}.
     * </p>
     *
     * @param trainerId the unique identifier of the trainer for whom the status is requested
     * @return a {@link ResponseEntity} containing the trainer's current status and HTTP 200 (OK) status
     *
     * @see TrainerStatusService#getStatus(String)
     */
    @GetMapping("/all/status")
    public ResponseEntity<String> getStatusForTrainer(@RequestParam @NotBlank String trainerId) {
        log.info("Request received to get status for trainer :: {}", trainerId);
        String response = trainerStatusService.getStatus(trainerId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Deletes the status record associated with a specific trainer.
     * <p>
     * Once deleted, the trainer may appear "UNAVAILABLE" until a status is reset.
     * </p>
     *
     * @param trainerId the unique identifier of the trainer whose status should be deleted
     * @return a {@link ResponseEntity} containing a confirmation message and HTTP 202 (Accepted) status
     *
     * @see TrainerStatusService#deleteStatus(String)
     */
    @DeleteMapping("/trainer/deleteStatus")
    public ResponseEntity<String> deleteStatusForTrainer(@RequestParam @NotBlank String trainerId) {
        log.info("Request received to delete status for trainer :: {}",trainerId);
        String response = trainerStatusService.deleteStatus(trainerId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
