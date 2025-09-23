package com.gym.trainerService.Services;

import com.gym.trainerService.Dto.TrainerMangementDto.Requests.SpecialityRequestDto;
import com.gym.trainerService.Dto.TrainerMangementDto.Requests.TrainerCreateRequestDto;
import com.gym.trainerService.Dto.TrainerMangementDto.Responses.AllTrainerResponseDto;
import com.gym.trainerService.Dto.TrainerMangementDto.Responses.TrainerResponseDto;
import com.gym.trainerService.Dto.TrainerMangementDto.Wrappers.AllTrainerResponseDtoWrapper;
import com.gym.trainerService.Exception.DuplicateSpecialtyFoundException;
import com.gym.trainerService.Exception.DuplicateTrainerFoundException;
import com.gym.trainerService.Exception.NoSpecialityFoundException;
import com.gym.trainerService.Exception.NoTrainerFoundException;
import com.gym.trainerService.Models.Specialities;
import com.gym.trainerService.Models.Trainer;
import com.gym.trainerService.Repositories.SpecialityRepository;
import com.gym.trainerService.Repositories.TrainerRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for Trainer Management.
 * Encapsulates business logic related to trainers and their specialities.
 * Handles persistence via repositories and manages cache for better
 * performance.
 * Responsibilities:
 * @author Arpan Das
 * @version 1.0
 * @since 1.0
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerManagementService {

    private final TrainerRepository trainerRepository;
    private final SpecialityRepository specialityRepository;

    /**
     * Creates a new trainer in the system with validation and cache eviction.
     * This method performs duplicate validation by checking both trainer ID and email
     * uniqueness before creating a new trainer record. Upon successful creation,
     * the all-trainers cache is evicted to maintain data consistency.
     *
     * @param requestDto the trainer creation request containing all required trainer information
     *                   including ID, personal details, contact information, and join date
     * @return {@link AllTrainerResponseDto} containing the created trainer's details
     * @throws DuplicateTrainerFoundException if a trainer with the same ID or email already exists
     * @throws IllegalArgumentException       if the request DTO contains invalid data
     * @see AllTrainerResponseDto
     * @see TrainerCreateRequestDto
     */
    @Transactional
    @CacheEvict(value = "AllTrainerCache", key = "'All'")
    public AllTrainerResponseDto createTrainer(TrainerCreateRequestDto requestDto) {
        boolean condition1 = trainerRepository.existsById(requestDto.getId()); // checking if any trainer exists
                                                                               // with same id
        boolean condition2 = trainerRepository.existsByEmail(requestDto.getEmail()); // checking if any trainer
                                                                                    // exists by same email
        if (condition1 && condition2) {
            throw new DuplicateTrainerFoundException("User already exists with id: " + requestDto.getId()); //throws
                                                                                                   // a custom exception
        }
        Trainer trainer = Trainer.builder()
                .trainerId(requestDto.getId())
                .firstName(requestDto.getFirstName()).lastName(requestDto.getLastName())
                .phone(requestDto.getPhone()).email(requestDto.getEmail())
                .gender(requestDto.getGender())
                .joinDate(requestDto.getJoinDate())
                .build();
        trainerRepository.save(trainer);
        log.info("Trainer saved with name {}", trainer.getFirstName() + " " + trainer.getLastName());
        return AllTrainerResponseDto.builder()
                .id(trainer.getTrainerId())
                .firstName(trainer.getFirstName()).lastName(trainer.getLastName())
                .email(trainer.getEmail()).phone(trainer.getPhone())
                .gender(trainer.getGender())
                .averageRating(trainer.getAverageRating())
                .lastLoginTime(trainer.getLastLogin())
                .build();
    }

    /**
     * Retrieves all trainers from the system with caching support.
     * This method fetches all trainer records from the database and transforms them
     * into response DTOs. The result is cached using Redis with the key 'All' to
     * improve performance on subsequent calls. The cache is automatically managed
     * and evicted when trainers are created or deleted.
     * @return {@link AllTrainerResponseDtoWrapper} containing a list of all trainers
     *         with their basic information including ID, name, contact details,
     *         profile image URL, average rating, and last login time
     * @see AllTrainerResponseDtoWrapper
     * @see AllTrainerResponseDto
     */
    @Cacheable(value = "AllTrainerCache", key = "'All'")
    public AllTrainerResponseDtoWrapper getAllTrainer() {
        // retrieving all the trainers from the db;
        List<Trainer> trainers = trainerRepository.findAll();
        // using stream with map to make the list of All TrainerResponseDto to return in the wrapper
        List<AllTrainerResponseDto> responseDtoList = trainers.stream()
                .map(dto -> AllTrainerResponseDto.builder()
                        .id(dto.getTrainerId())
                        .imageUrl(dto.getTrainerProfileImageUrl())
                        .firstName(dto.getFirstName())
                        .lastName(dto.getLastName())
                        .email(dto.getEmail()).phone(dto.getPhone())
                        .gender(dto.getGender())
                        .averageRating(dto.getAverageRating())
                        .lastLoginTime(dto.getLastLogin())
                        .build())
                .toList();
        // returning all the wrapper dto
        return AllTrainerResponseDtoWrapper.builder()
                .allTrainerResponseDtoWrappers(responseDtoList)
                .build();
    }

    /**
     * Retrieves detailed information for a specific trainer by their unique identifier.
     * This method fetches a trainer's complete profile including personal information,
     * contact details, specialities, ratings, and last login time. The result is
     * cached per trainer ID to optimize performance for frequently accessed trainer profiles.
     * @param trainerId the unique identifier of the trainer to retrieve
     * @return {@link TrainerResponseDto} containing comprehensive trainer details
     *         including specialities list, profile information, and statistics
     * @throws NoTrainerFoundException if no trainer exists with the specified ID
     * @see TrainerResponseDto
     * @since 1.0
     */
    @Cacheable(value = "trainerCache", key = "#trainerId")
    public TrainerResponseDto getTrainerById(String trainerId) {
        // fetching the trainer by an id
        Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new NoTrainerFoundException(
                        "No trainer found with the id: " + trainerId)); // if no trainer found
                                                                        // then throws a custom exception
                return trainerResponseDtoBuilder(trainer);
        }

    /**
     * Deletes a trainer from the system by their unique identifier with cache management.
     * This method removes a trainer record from the database and ensures cache consistency
     * by evicting both the individual trainer cache entry and the all-trainers cache.
     * The operation is wrapped in a transaction to ensure data integrity and rollback
     * capability in case of failures.
     * @param trainerId the unique identifier of the trainer to delete
     * @return a success message confirming the deletion with the trainer ID
     * @throws DataAccessException if there's an error accessing the database
     * @see org.springframework.dao.DataAccessException
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "trainerCache", key = "#trainerId"),
            @CacheEvict(value = "AllTrainerCache", key = "'All'")
    })
    public String deleteTrainerById(String trainerId) {
        log.info("Request received in service layer");
        trainerRepository.deleteById(trainerId); // deleting the id of a trainer from database
        log.info("Deleted successfully for trainer id---> {}", trainerId);
        return "Successfully deleted trainer for id ===> " + trainerId; // returning a success message
    }

    /**
     * Adds new specialities to an existing trainer's profile.
     * This method validates that the trainer exists and checks for duplicate specialities
     * before adding new ones. Each speciality in the request is validated against existing
     * specialities for the trainer to prevent duplicates. The trainer's cache is evicted
     * to ensure fresh data on subsequent retrievals.
     *
     * @param trainerId  the unique identifier of the trainer to update
     * @param requestDto the request containing a list of new specialities to add
     * @return {@link TrainerResponseDto} containing the updated trainer information
     * including the newly added specialities
     * @throws NoTrainerFoundException          if no trainer exists with the specified ID
     * @throws DuplicateSpecialtyFoundException if any of the specialities already exist for the trainer
     * @throws javax                            validation if the request DTO fails validation
     * @see SpecialityRequestDto
     * @see TrainerResponseDto
     */
    @Transactional
    @CacheEvict(value = "trainerCache", key = "#trainerId")
    public TrainerResponseDto addSpecialityForTrainer(String trainerId, @Valid SpecialityRequestDto requestDto) {
        Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new NoTrainerFoundException(
                        "No trainer found with the id: " + trainerId));
        log.info("trainer fetched from db with id: {}", trainer.getTrainerId());
        List<Specialities> specialitiesList = specialityRepository.findByTrainerId(trainer.getTrainerId());
        for (Specialities specialities : specialitiesList) {
            if (requestDto.getSpecialityList().contains(specialities.getSpeciality())) {
                throw new DuplicateSpecialtyFoundException(
                        "A speciality already found named: " + specialities.getSpeciality());
            }
        }
        requestDto.getSpecialityList().forEach(speciality -> specialityRepository.save(
                Specialities.builder()
                        .speciality(speciality)
                        .trainerId(trainer.getTrainerId())
                        .build()));
        return trainerResponseDtoBuilder(trainer);
    }
    /**
     * Updates an existing speciality name for a specific trainer.
     * This method allows renaming a trainer's speciality from an old name to a new name.
     * It validates that both the trainer and the old speciality exist before performing
     * the update. The trainer's cache is evicted to maintain data consistency.
     * @param trainerId the unique identifier of the trainer whose speciality is being updated
     * @param oldSpecialityName the current name of the speciality to be changed
     * @param newSpecialityName the new name for the speciality
     * @return {@link TrainerResponseDto} containing the updated trainer information
     *         with the renamed speciality
     * @throws NoTrainerFoundException if no trainer exists with the specified ID
     * @throws NoSpecialityFoundException if the old speciality name doesn't exist for the trainer
     * @see TrainerResponseDto
     */
    @Transactional
    @CacheEvict(value = "trainerCache", key = "#trainerId")
    public TrainerResponseDto changeSpecialityFromOldNameToNewName(String trainerId,
                                                                   String oldSpecialityName,
                                                                   String newSpecialityName) {
        Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new NoTrainerFoundException(
                        "No trainer found with the id: " + trainerId));
        log.info("Successfully fetched trainer from db with name {} {}", trainer.getFirstName(),
                trainer.getLastName());
        Specialities specialities = specialityRepository.findSpecialityByTrainerIdAndName(trainerId,
                oldSpecialityName);
        if (specialities == null) {
            throw new NoSpecialityFoundException("No speciality found with name " + oldSpecialityName);
        }
        log.info("Successfully fetched specialityName {}", specialities.getSpeciality());
        specialities.setSpeciality(newSpecialityName);
        specialityRepository.save(specialities);
        log.info("Successfully saved speciality with name {} ", specialities.getSpeciality());
        return trainerResponseDtoBuilder(trainer);
    }
    /**
     * Deletes a specific speciality from a trainer's profile by speciality name.
     * This method removes a speciality from a trainer's profile based on the speciality name.
     * It returns a success or failure message based on whether any records were affected.
     * The trainer's cache is evicted to ensure data consistency.
     * @param trainerId the unique identifier of the trainer whose speciality is being deleted
     * @param specialityName the name of the speciality to be removed
     * @return a string message indicating the success or failure of the deletion operation
     *         including the number of affected rows or a not-found message
     * @see org.springframework.dao.DataAccessException
     */
    @Transactional
    @CacheEvict(value = "trainerCache", key = "#trainerId")
    public String deleteSpecializationByName(String trainerId, String specialityName) {
        log.info("Request received to delete speciality with name {}", specialityName);
        int effectedRows = specialityRepository.deleteByTrainerIdWithName(trainerId, specialityName);
        log.info("Successfully deleted and {} rows effected", effectedRows);
        return effectedRows > 0 ? "Successfully deleted speciality of name " + specialityName
                : "No speciality found with the name " + specialityName;
    }
    /**
     * Private utility method to build a comprehensive TrainerResponseDto from a Trainer entity.
     * This method constructs a complete trainer response DTO by combining trainer information
     * with their associated specialities. It reduces code duplication across multiple methods
     * that need to return trainer details.
     * @param trainer the Trainer entity to convert to a response DTO
     * @return {@link TrainerResponseDto} containing all trainer information including
     *         personal details, contact information, specialities, and statistics
     * @see TrainerResponseDto
     * @see Trainer
     */
    private TrainerResponseDto trainerResponseDtoBuilder(Trainer trainer) {
        List<Specialities> specialities = specialityRepository.findByTrainerId(trainer.getTrainerId());
        return TrainerResponseDto.builder()
                .trainerId(trainer.getTrainerId())
                .trainerProfileImageUrl(trainer.getTrainerProfileImageUrl())
                .firstName(trainer.getFirstName()).lastName(trainer.getLastName())
                .emailId(trainer.getEmail()).phone(trainer.getPhone())
                .gender(trainer.getGender())
                .specialities(specialities)
                .lastLoginTime(trainer.getLastLogin())
                .averageRating(trainer.getAverageRating())
                .build();
    }

}
