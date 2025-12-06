package com.gym.trainerService.Services.TrainerServices;

import com.gym.trainerService.Dto.MemberDtos.Responses.SessionMatrixInfo;
import com.gym.trainerService.Dto.TrainerMangementDto.Requests.SpecialityResponseDto;
import com.gym.trainerService.Dto.TrainerMangementDto.Requests.TrainerAboutRequestDto;
import com.gym.trainerService.Dto.TrainerMangementDto.Requests.TrainerCreateRequestDto;
import com.gym.trainerService.Dto.TrainerMangementDto.Responses.*;
import com.gym.trainerService.Dto.TrainerMangementDto.Wrappers.AllPublicTrainerInfoResponseWrapperDto;
import com.gym.trainerService.Dto.TrainerMangementDto.Wrappers.AllTrainerResponseDtoWrapper;
import com.gym.trainerService.Dto.TrainerReviewDto.Responses.RatingMatrixInfo;
import com.gym.trainerService.Exception.Custom.*;
import com.gym.trainerService.Models.Specialities;
import com.gym.trainerService.Models.Trainer;
import com.gym.trainerService.Repositories.MemberRepository;
import com.gym.trainerService.Repositories.ReviewRepository;
import com.gym.trainerService.Repositories.SpecialityRepository;
import com.gym.trainerService.Repositories.TrainerRepository;
import com.gym.trainerService.Services.MemberServices.SessionManagementService;
import com.gym.trainerService.Services.OtherServices.SpecialityService;
import com.gym.trainerService.Utils.CustomAnnotations.Annotations.LogRequestTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    private final SpecialityService specialityService;
    private final SessionManagementService sessionManagementService;
    private final TrainerReviewService reviewService;
    private final TrainerRepository trainerRepository;
    private final SpecialityRepository specialityRepository;
    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
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
    @Caching( evict = {
            @CacheEvict(value = "AllTrainerCache", key = "'All'"),
            @CacheEvict (value = "trainerBasic", key = "'allTrainers'")
    }
    )
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
                .firstName(trainer.getFirstName())
                .lastName(trainer.getLastName())
                .email(trainer.getEmail())
                .phone(trainer.getPhone())
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
                        .email(dto.getEmail())
                        .phone(dto.getPhone())
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
            @CacheEvict(value = "AllTrainerCache", key = "'All'"),
            @CacheEvict(value = "trainerBasic", key = "'allTrainers'")
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
     * @param speciality the request containing a string of a  new speciality to add
     * @return {@link SpecialityResponseDto} containing the updated trainer information
     * including the newly added specialities
     * @throws NoTrainerFoundException          if no trainer exists with the specified ID
     * @throws DuplicateSpecialtyFoundException if any of the specialities already exist for the trainer
     * @throws javax                            validation if the request DTO fails validation
     * @see SpecialityResponseDto
     */
    @LogRequestTime
    @Transactional
    @CachePut(value = "speciality", key = "#trainerId")
    public SpecialityResponseDto addSpecialityForTrainer(String trainerId, String speciality) {
        log.info("Request received to add speciality '{}' for trainerId {}", speciality, trainerId);
        int count = specialityRepository.getSpecialityCount(trainerId);
        if(count>=5) throw new MaxiMumSpecialityAchivedException("You Can Not Have More Specialites than 5");
        String normalized = specialityService.normalize(speciality);
        log.debug("Normalized speciality: {}", normalized);

        if (!specialityService.isValidSpeciality(normalized)) {
            log.error("Invalid speciality '{}' provided for trainer {}", speciality, trainerId);
            throw new InvalidSpecialityException("No Such Speciality Found. Kindly Provide a Valid Speciality");
        }
        Trainer trainer = getById(trainerId);
        log.info("Trainer fetched successfully: {}", trainer.getTrainerId());

        List<Specialities> existingSpecs = specialityRepository.findAllTrainerId(trainerId);
        log.debug("Existing specialities count for trainer {}: {}", trainerId, existingSpecs.size());

        boolean alreadyExists = existingSpecs.stream()
                .anyMatch(s -> s.getSpeciality().equalsIgnoreCase(normalized));

        if (alreadyExists) {
            log.warn("Duplicate speciality '{}' found for trainer {}", normalized, trainerId);
            throw new DuplicateSpecialtyFoundException(
                    "This speciality is already associated with your ID. Please choose another one."
            );
        }

        Specialities newSpeciality = Specialities.builder()
                .trainerId(trainerId)
                .speciality(normalized)
                .build();

        specialityRepository.save(newSpeciality);
        log.info("Successfully added speciality '{}' for trainer {}", normalized, trainerId);

        SpecialityResponseDto response = getSpecialityByTrainerId(trainer.getTrainerId());

        log.debug("Response prepared: {}", response);

        return response;
    }

    @Cacheable(value = "speciality",key = "#trainerId")
    public SpecialityResponseDto getSpecialityByTrainerId(String trainerId) {
        List<Specialities> specialities = specialityRepository.findAllTrainerId(trainerId);
        return SpecialityResponseDto.builder()
                .specialityList(specialities.stream().map(Specialities::getSpeciality).toList())
                .build();
    }

    /**
     * Updates an existing speciality name for a specific trainer.
     * This method allows renaming a trainer's speciality from an old name to a new name.
     * It validates that both the trainer and the old speciality exist before performing
     * the update. The trainer's cache is evicted to maintain data consistency.
     * @param trainerId the unique identifier of the trainer whose speciality is being updated
     * @param oldSpecialityName the current name of the speciality to be changed
     * @param newSpecialityName the new name for the speciality
     * @return {@link SpecialityResponseDto} containing the updated trainer information
     *         with the renamed speciality
     * @throws NoTrainerFoundException if no trainer exists with the specified ID
     * @throws NoSpecialityFoundException if the old speciality name doesn't exist for the trainer
     * @see SpecialityResponseDto
     */
    @Transactional
    @CachePut(value = "speciality", key = "#trainerId")
    public SpecialityResponseDto changeSpecialityFromOldNameToNewName(String trainerId,
                                                                   String oldSpecialityName,
                                                                   String newSpecialityName) {
        Trainer trainer = getById(trainerId);
        log.info("Successfully fetched trainer from db with name {} {}", trainer.getFirstName(),
                trainer.getLastName());
        Specialities specialities = specialityRepository.findSpecialityByTrainerIdAndName(trainerId,
                oldSpecialityName);
        if (specialities == null) {
            throw new NoSpecialityFoundException("No speciality found with name " + oldSpecialityName);
        }
        log.info("Successfully fetched specialityName {}", specialities.getSpeciality());
        String normalize = specialityService.normalize(newSpecialityName);
        if(!specialityService.isValidSpeciality(newSpecialityName)){
            throw new InvalidSpecialityException("No Such Speciality Found. Kindly Provide a Valid Speciality");
        }
        specialities.setSpeciality(normalize);
        specialityRepository.save(specialities);
        log.info("Successfully saved speciality with name {} ", specialities.getSpeciality());
        return getSpecialityByTrainerId(trainer.getTrainerId());
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
    @Caching(evict = {
            @CacheEvict(value = "speciality", key = "#trainerId"),
            @CacheEvict(value = "trainerCache", key = "#trainerId")
    })
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
        return TrainerResponseDto.builder()
                .trainerId(trainer.getTrainerId())
                .trainerProfileImageUrl(trainer.getTrainerProfileImageUrl())
                .firstName(trainer.getFirstName())
                .lastName(trainer.getLastName())
                .emailId(trainer.getEmail())
                .phone(trainer.getPhone())
                .gender(trainer.getGender())
                .lastLoginTime(trainer.getLastLogin())
                .averageRating(trainer.getAverageRating())
                .build();
    }

    /**
     * This method is responsible to show trainer's all basic details in home page
     * to show users basic details of every trainer
     * @return {@link AllPublicTrainerInfoResponseWrapperDto} which is contains a List of
     * @see PublicTrainerInfoResponseDto
     * which has basic information of trainer {@link Trainer}
     * used java stream with make to build readable code and easy to understand
     * */
    @LogRequestTime
    @Cacheable(value = "trainerBasic", key = "'allTrainers'")
    public AllPublicTrainerInfoResponseWrapperDto getAllTrainerBasicInfo() {
        log.info("🚀 Starting getAllTrainerBasicInfo method. Checking cache for 'allTrainers'.");

        List<Trainer> trainerList = trainerRepository.findAll();
        log.info("📚 Fetched {} trainers from the database.", trainerList.size());

        List<Object[]> reviewCounts = reviewRepository.getReviewCountsForAllTrainers();
        log.info("📊 Fetched review counts for {} distinct trainers via GROUP BY query.", reviewCounts.size());

        Map<String, Long> reviewMap = reviewCounts.stream()
                .collect(Collectors.toMap(
                        r -> (String) r[0],
                        r -> (Long) r[1]));
        log.debug("🗺️ Review counts mapped for fast lookup. Map size: {}", reviewMap.size());

        List<Object[]> clientCount = memberRepository.getClientCountForAllTrainers();
        log.info("👥 Fetched client counts for {} distinct trainers via GROUP BY query.", clientCount.size());

        Map<String, Long> clientCountMap = clientCount.stream()
                .collect(Collectors.toMap(
                        c -> (String) c[0],
                        c -> (Long) c[1]));
        log.debug("🗺️ Client counts mapped for fast lookup. Map size: {}", clientCountMap.size());

        List<Specialities> specialities = specialityRepository.findAll();
        log.info("Fetched {} no of specialites for all trainers",specialities.size());

        Map<String ,List<Specialities>> specialitiesMap = specialities.stream()
                .collect(Collectors.groupingBy(Specialities::getTrainerId));

        List<PublicTrainerInfoResponseDto> resultList = trainerList.stream()
                        .map(t->{
                            List<Specialities> trainerSpecialization = specialitiesMap
                                    .getOrDefault(t.getTrainerId(), List.of());
                            List<String> specialityNames = trainerSpecialization.stream()
                                    .map(Specialities::getSpeciality).toList();
                            log.info("Collect {} specialites for trainer {} {}"
                                    ,specialityNames.size(),t.getFirstName(),t.getLastName());

                            return PublicTrainerInfoResponseDto.builder()
                                    .id(t.getTrainerId())
                                    .firstName(t.getFirstName())
                                    .lastName(t.getLastName())
                                    .about(t.getAbout())
                                    .clientCount(clientCountMap
                                            .getOrDefault(t.getTrainerId(),0L).intValue())
                                    .email(t.getEmail())
                                    .gender(t.getGender())
                                    .averageRating(t.getAverageRating())
                                    .reviewCount(reviewMap
                                            .getOrDefault(t.getTrainerId(),0L).intValue())
                                    .specialities(specialityNames)
                                    .build();
                        }).toList();

        log.info("✅ Successfully mapped {} Trainer entities to DTOs with aggregated counts.", resultList.size());

        return AllPublicTrainerInfoResponseWrapperDto.builder()
                .publicTrainerInfoResponseDtoList(resultList)
                .build();
    }

    @Transactional
    @LogRequestTime
    @Caching(evict = {
           @CacheEvict(value = "trainer",key = "#requestDto.trainerId")
    }, put = {
            @CachePut(value = "about", key = "#requestDto.trainerId")
    })
    public String setTrainerAbout(TrainerAboutRequestDto requestDto) {
        Trainer trainer = getById(requestDto.getTrainerId());
        log.info("Successfully fetched 👟👟 trainer from db/cache {} {}",
                trainer.getFirstName(),trainer.getLastName());
        trainer.setAbout(requestDto.getAbout());
        trainerRepository.save(trainer);
        log.info("Successfully saved about for trainer {}",requestDto.getTrainerId());
        return trainer.getAbout();
    }

    @LogRequestTime
    @Cacheable(value = "about", key = "#trainerId")
    public String getTrainerAboutById(String trainerId) {
        String response = getById(trainerId).getAbout();
        log.info("Retrieved about for trainer {} of length {}",trainerId,response.length());
        return response;
    }

    @Cacheable(value = "trainer",key = "#id")
    public Trainer getById(String id) {
        return trainerRepository.findById(id)
                .orElseThrow(() -> new NoTrainerFoundException(
                "No trainer found with the id: " + id));
    }

    @LogRequestTime
    @Cacheable(value = "DashboardInfo",key = "#trainerId")
    public TrainerDashBoardInfoResponseDto getTrainerDashBoardInfo(String trainerId) {
        Trainer trainer = getById(trainerId);
        if(trainer.isFrozen()) {
            log.info("🥶🥶 Can not Proceed Request because account of {} {} is frozen",
                    trainer.getFirstName(),trainer.getLastName());
            throw new UnAuthorizedRequestException("Your Account Is Frozen Can not Proceed Request");
        }
        LocalDate current = LocalDate.now().withDayOfMonth(1);
        LocalDate previous = current.minusDays(1);
        ClientMatrixInfo clientMatrixInfo = getClientMatricesInfo(current,previous,trainer.getTrainerId());
        SessionMatrixInfo sessionMatrixInfo = sessionManagementService.getSessionMatrix(trainer.getTrainerId());
        RatingMatrixInfo ratingMatrixInfo = reviewService.getRatingMatrix(trainer.getTrainerId());
        log.info("🗃️🗃️🗃️ Sending response for {} {} for trainer dashboard info",
                trainer.getFirstName(),trainer.getLastName());
        return TrainerDashBoardInfoResponseDto.builder()
                .clientMatrixInfo(clientMatrixInfo)
                .sessionMatrixInfo(sessionMatrixInfo)
                .ratingMatrixInfo(ratingMatrixInfo)
                .build();
    }

    @Cacheable(value = "ClientMatrix",key = "#trainerId")
    private ClientMatrixInfo getClientMatricesInfo(LocalDate current, LocalDate previous, String trainerId) {
        log.info("📊📊 Request received to get client matrix for trainer {}",trainerId);
        int currentClientCount = memberRepository.countCurrentMembers(current).intValue();
        int previousClientCount = memberRepository.countMembersEligibleAtLastMonthEnd(previous).intValue();

        int change = (currentClientCount-previousClientCount);
        double percentage = ((double) change /currentClientCount)*100;
        log.info("current client count in {} and last month's was {} and the change is {}%",
                currentClientCount,previousClientCount,percentage);

        return ClientMatrixInfo.builder()
                .currentMonthClientCount(currentClientCount)
                .previousMonthClientCount(previousClientCount)
                .change(percentage)
                .build();
    }


}
