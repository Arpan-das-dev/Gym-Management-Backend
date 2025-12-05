package com.gym.trainerService.Controllers;

import com.gym.trainerService.Dto.MemberDtos.Responses.GenericResponse;

import com.gym.trainerService.Dto.TrainerMangementDto.Requests.SpecialityResponseDto;
import com.gym.trainerService.Dto.TrainerMangementDto.Requests.TrainerAboutRequestDto;
import com.gym.trainerService.Dto.TrainerMangementDto.Requests.TrainerCreateRequestDto;
import com.gym.trainerService.Dto.TrainerMangementDto.Responses.AllTrainerResponseDto;
import com.gym.trainerService.Dto.TrainerMangementDto.Responses.PublicTrainerInfoResponseDto;
import com.gym.trainerService.Dto.TrainerMangementDto.Responses.TrainerDashBoardInfoResponseDto;
import com.gym.trainerService.Dto.TrainerMangementDto.Responses.TrainerResponseDto;
import com.gym.trainerService.Dto.TrainerMangementDto.Wrappers.AllPublicTrainerInfoResponseWrapperDto;
import com.gym.trainerService.Dto.TrainerMangementDto.Wrappers.AllTrainerResponseDtoWrapper;
import com.gym.trainerService.Exception.Custom.DuplicateSpecialtyFoundException;
import com.gym.trainerService.Exception.Custom.NoSpecialityFoundException;
import com.gym.trainerService.Exception.Custom.NoTrainerFoundException;
import com.gym.trainerService.Services.OtherServices.SpecialityService;
import com.gym.trainerService.Services.TrainerServices.TrainerManagementService;
import com.gym.trainerService.Utils.CustomAnnotations.Annotations.LogExecutionTime;
import com.gym.trainerService.Utils.CustomAnnotations.Annotations.LogRequestTime;
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
    private final SpecialityService specialityService;

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
    @LogExecutionTime
    @PostMapping("/create")
    public ResponseEntity<AllTrainerResponseDto> createTrainer( @RequestBody TrainerCreateRequestDto requestDto) {
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
    @LogExecutionTime
    @GetMapping("/trainer/get")
    public ResponseEntity<TrainerResponseDto> getTrainerById(@RequestParam String trainerId) {
        log.info("Request received to get info of trainer with id {}", trainerId);
        TrainerResponseDto responseDto = trainerManagementService.getTrainerById(trainerId);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }


    @LogExecutionTime
    @GetMapping("/trainer/dashboard")
    public ResponseEntity<TrainerDashBoardInfoResponseDto> getTrainerDashboardInfo(
            @RequestParam @NotBlank(message = "Please Provide a Valid Id to Proceed Request") String trainerId) {
        log.info("®️®️ Request received to get info for trainer's dashboard for trainer {}",trainerId);
        TrainerDashBoardInfoResponseDto response = trainerManagementService.getTrainerDashBoardInfo(trainerId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    /**
     * Retrieves basic information for all trainers
     *
     * @return {@link  AllPublicTrainerInfoResponseWrapperDto} containing a list of all trainer's basic details
     * @see com.gym.trainerService.Dto.TrainerMangementDto.Responses.PublicTrainerInfoResponseDto
     *
     */

    @LogExecutionTime
    @GetMapping("/all/getTrainers")
    public  ResponseEntity<AllPublicTrainerInfoResponseWrapperDto> getAllBasicTrainerInfo() {
        log.info("ℹ️ℹ️ Request received to get all trainer's basic info for HomePage");
        AllPublicTrainerInfoResponseWrapperDto responseWrapperDto = trainerManagementService
                .getAllTrainerBasicInfo();
        log.info("Sending response of {} no of trainer"
                ,responseWrapperDto.getPublicTrainerInfoResponseDtoList().size());
        return ResponseEntity.status(HttpStatus.OK).body(responseWrapperDto);
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
    @LogExecutionTime
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
    @LogExecutionTime
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteTrainerById(@RequestParam String trainerId) {
        log.info("Request received to delete trainer by id---> {}", trainerId);
        String responseDto = trainerManagementService.deleteTrainerById(trainerId);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    /**
     *
     * */
    @LogRequestTime
    @GetMapping("/trainer/getSpecialites")
    public ResponseEntity<SpecialityResponseDto> getAllSpecialites() {
        log.info("Request received to get All Specialities");
        SpecialityResponseDto responseDto = specialityService.getAllSpecialites();
        log.info("Returning total {} no of specialities ",responseDto.getSpecialityList().size());
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
     * @param speciality the new speciality
     * @return {@link SpecialityResponseDto} with updated trainer information
     * @throws NoTrainerFoundException          if the trainer does not exist
     * @throws IllegalArgumentException when the validation fails for parameter
     * @throws DuplicateSpecialtyFoundException if a speciality already exists
     * @see TrainerManagementService#addSpecialityForTrainer(String, String)
     */
    @LogExecutionTime
    @PostMapping("/trainer/speciality")
    public ResponseEntity<SpecialityResponseDto> addSpecialization(
            @RequestParam @NotBlank(message = "Can not Proceed With Blank Input") String trainerId,
            @RequestParam @NotBlank(message = "Can not Proceed With Blank Input") String speciality) {
        log.info("Request received to add SpecialityResponseDto specialization for trainer id ---> {}", trainerId);
        SpecialityResponseDto responseDto = trainerManagementService.addSpecialityForTrainer(trainerId, speciality);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }
    /**
     * Updates an existing speciality name for a trainer.
     *
     * @param trainerId         the unique identifier of the trainer
     * @param oldSpecialityName the current name of the speciality to update
     * @param newSpecialityName the new name for the speciality
     * @return {@link SpecialityResponseDto} with updated trainer details
     * @throws NoTrainerFoundException     if the trainer does not exist
     * @throws NoSpecialityFoundException  if the old speciality name is not found
     * @see TrainerManagementService#changeSpecialityFromOldNameToNewName(String, String, String)
     */
    @LogExecutionTime
    @PutMapping("/trainer/update")
    public ResponseEntity<SpecialityResponseDto> updateSpecializationByName(
            @RequestParam
            @NotBlank(message = "Can not Proceed with Empty Input Kindly Enter a Valid Data to Proceed") String trainerId,
            @RequestParam @NotBlank(message = "Old Speciality Can not Be Empty") String oldSpecialityName,
            @RequestParam @NotBlank(message = "New Specialty Can not Be Empty") String newSpecialityName)
    {
        log.info("Successfully received to change speciality name from {} to {}", oldSpecialityName, newSpecialityName);
        SpecialityResponseDto responseDto = trainerManagementService
                .changeSpecialityFromOldNameToNewName(trainerId, oldSpecialityName, newSpecialityName);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(responseDto);
    }
    /**
     * Get Speciality for a trainer
     * @param trainerId the unique identifier of the trainer
     * @throws org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException if the validation of the parameter fails
     * @throws NoTrainerFoundException if the trainer does not exist
     * @return {@link SpecialityResponseDto} contains all the speciality a trainer can have
     * @see TrainerManagementService#getSpecialityByTrainerId(String)
     * */
    @LogRequestTime
    @GetMapping("/all/getSpeciality")
    public ResponseEntity<SpecialityResponseDto> getSpecialityByTrainerId(
            @RequestParam
            @NotBlank(message = "Can not Proceed with Empty Input Kindly Enter a Valid Data to Proceed")
            String trainerId) {
        log.info("Request received to get specialties for trainer {}",trainerId);
        SpecialityResponseDto response = trainerManagementService.getSpecialityByTrainerId(trainerId);
        log.info("sending {} no of specialties for trainer {}",response.getSpecialityList().size(),trainerId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
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
    @LogExecutionTime
    @DeleteMapping("/trainer/delete")
    public ResponseEntity<GenericResponse> deleteSpecializationByName(@RequestParam @NotBlank String trainerId,
                                                             @RequestParam @NotBlank String specialityName)
    {
        log.info("Request received to delete speciality {} for trainer {} ", specialityName, trainerId);
        String response = trainerManagementService.deleteSpecializationByName(trainerId, specialityName);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new GenericResponse(response));
    }

    @LogExecutionTime
    @PostMapping("/trainer/setAbout")
    public ResponseEntity<GenericResponse> setAboutForTrainer(@RequestBody TrainerAboutRequestDto requestDto){
        log.info("Request reached for set trainer's about for trainer {}",requestDto.getTrainerId());
        String response = trainerManagementService.setTrainerAbout(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new GenericResponse(response));
    }

    @LogExecutionTime
    @GetMapping("/all/getAbout")
    public ResponseEntity<GenericResponse> getTrainerAboutById(String trainerId) {
        log.info("Request reached for get about for trainer {}",trainerId);
        String response = trainerManagementService.getTrainerAboutById(trainerId);
        return ResponseEntity.status(HttpStatus.OK).body(new GenericResponse(response));
    }
}
