package com.gym.trainerService.Controllers;

import com.gym.trainerService.Dto.TrainerMangementDto.Requests.SpecialityRequestDto;
import com.gym.trainerService.Dto.TrainerMangementDto.Requests.TrainerCreateRequestDto;
import com.gym.trainerService.Dto.TrainerMangementDto.Responses.AllTrainerResponseDto;
import com.gym.trainerService.Dto.TrainerMangementDto.Responses.TrainerResponseDto;
import com.gym.trainerService.Dto.TrainerMangementDto.Wrappers.AllTrainerResponseDtoWrapper;
import com.gym.trainerService.Exception.DuplicateSpecialtyFoundException;
import com.gym.trainerService.Exception.NoSpecialityFoundException;
import com.gym.trainerService.Exception.NoTrainerFoundException;
import com.gym.trainerService.Services.TrainerServices.TrainerManagementService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for managing Trainer-related operations.
 * <p>
 * This controller provides REST endpoints for CRUD operations on trainers
 * and their associated specialities. It acts as an entry point for client
 * requests, delegating business logic to the {@link TrainerManagementService}.
 * </p>
 *
 * <p><b>Responsibilities:</b></p>
 * <ul>
 *     <li>Create trainers</li>
 *     <li>Retrieve trainer details (single & all)</li>
 *     <li>Delete trainers</li>
 *     <li>Manage trainer specialities (add, update, delete)</li>
 * </ul>
 *
 * <p>All responses are wrapped in {@link ResponseEntity} with appropriate
 * HTTP status codes. Logging is enabled at each entry point for
 * traceability and debugging.</p>
 *
 * @author Arpan Das
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("${trainer-service.Base_Url}")
@Validated
public class TrainerManagementController {

    private final TrainerManagementService trainerManagementService;

    /**
     * Creates a new trainer in the system.
     * <p>
     * Delegates the creation logic to the service layer where validation and
     * persistence are handled. Returns the created trainer’s details on success.
     * </p>
     *
     * @param requestDto {@link TrainerCreateRequestDto} containing trainer details
     * @return {@link AllTrainerResponseDto} with the created trainer information
     * @see TrainerManagementService#createTrainer(TrainerCreateRequestDto)
     */
    @PostMapping("/create")
    public ResponseEntity<AllTrainerResponseDto> createTrainer(@Valid @RequestBody TrainerCreateRequestDto requestDto) {
        log.info("Request received to create trainer with id {}", requestDto.getId());
        AllTrainerResponseDto responseDto = trainerManagementService.createTrainer(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    /**
     * Retrieves detailed information for a trainer by ID.
     *
     * @param trainerId the unique identifier of the trainer to fetch
     * @return {@link TrainerResponseDto} containing trainer details
     * @throws NoTrainerFoundException if no trainer exists with the provided ID
     * @see TrainerManagementService#getTrainerById(String)
     */
    @GetMapping("/all/get")
    public ResponseEntity<TrainerResponseDto> getTrainerById(@RequestParam String trainerId) {
        log.info("Request received to get info of trainer with id {}", trainerId);
        TrainerResponseDto responseDto = trainerManagementService.getTrainerById(trainerId);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    /**
     * Retrieves all trainers from the system.
     * <p>
     * Returns a wrapper containing a list of all trainer response DTOs.
     * Useful for administrative or listing purposes.
     * </p>
     *
     * @return {@link AllTrainerResponseDtoWrapper} containing all trainers
     * @see TrainerManagementService#getAllTrainer()
     */
    @GetMapping("/admin/getAll")
    public ResponseEntity<AllTrainerResponseDtoWrapper> getAllTrainer() {
        log.info("RequestReceived to get all trainerInfo");
        AllTrainerResponseDtoWrapper responseDto = trainerManagementService.getAllTrainer();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(responseDto);
    }

    /**
     * Deletes a trainer by their unique identifier.
     *
     * @param trainerId the unique identifier of the trainer to delete
     * @return a success message indicating the trainer was deleted
     * @throws NoTrainerFoundException if the trainer does not exist
     * @see TrainerManagementService#deleteTrainerById(String)
     */
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteTrainerById(@RequestParam String trainerId) {
        log.info("Request received to delete trainer by id---> {}", trainerId);
        String responseDto = trainerManagementService.deleteTrainerById(trainerId);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    /**
     * Adds one or more specialities to a trainer.
     * <p>
     * Validates the request and ensures no duplicate specialities
     * are added for the given trainer.
     * </p>
     *
     * @param trainerId  the unique identifier of the trainer
     * @param requestDto {@link SpecialityRequestDto} containing the new specialities
     * @return {@link TrainerResponseDto} with updated trainer information
     * @throws NoTrainerFoundException          if the trainer does not exist
     * @throws DuplicateSpecialtyFoundException if a speciality already exists
     * @see TrainerManagementService#addSpecialityForTrainer(String, SpecialityRequestDto)
     */
    @PostMapping("/trainer/speciality")
    public ResponseEntity<TrainerResponseDto> addSpecialization(@RequestParam String trainerId,
                                                                @Valid @RequestBody SpecialityRequestDto requestDto) {
        log.info("Request received to add a specialization for trainer id ---> {}", trainerId);
        TrainerResponseDto responseDto = trainerManagementService.addSpecialityForTrainer(trainerId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }
    /**
     * Updates an existing speciality name for a trainer.
     *
     * @param trainerId         the unique identifier of the trainer
     * @param oldSpecialityName the current name of the speciality to update
     * @param newSpecialityName the new name for the speciality
     * @return {@link TrainerResponseDto} with updated trainer details
     * @throws NoTrainerFoundException     if the trainer does not exist
     * @throws NoSpecialityFoundException  if the old speciality name is not found
     * @see TrainerManagementService#changeSpecialityFromOldNameToNewName(String, String, String)
     */
    @PutMapping("/trainer/update")
    public ResponseEntity<TrainerResponseDto> updateSpecializationByName(@RequestParam @NotBlank String trainerId,
                                                                         @RequestParam @NotBlank String oldSpecialityName,
                                                                         @RequestParam @NotBlank String newSpecialityName)
    {
        log.info("Successfully received to change speciality name from {} to {}", oldSpecialityName, newSpecialityName);
        TrainerResponseDto responseDto = trainerManagementService
                .changeSpecialityFromOldNameToNewName(trainerId, oldSpecialityName, newSpecialityName);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(responseDto);
    }
    /**
     * Deletes a speciality from a trainer's profile.
     *
     * @param trainerId      the unique identifier of the trainer
     * @param specialityName the name of the speciality to remove
     * @return a message indicating whether the deletion was successful
     * @throws NoTrainerFoundException if the trainer does not exist
     * @see TrainerManagementService#deleteSpecializationByName(String, String)
     */
    @DeleteMapping("/trainer/delete")
    public ResponseEntity<String> deleteSpecializationByName(@RequestParam @NotBlank String trainerId,
                                                             @RequestParam @NotBlank String specialityName)
    {
        log.info("Request received to delete speciality {} for trainer {} ", specialityName, trainerId);
        String response = trainerManagementService.deleteSpecializationByName(trainerId, specialityName);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
