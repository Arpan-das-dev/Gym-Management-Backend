package com.gym.member_service.Services.FitnessServices;

import com.gym.member_service.Dto.MemberFitDtos.Requests.PrProgressRequestDto;
import com.gym.member_service.Dto.MemberFitDtos.Requests.UpdatePrRequestDto;
import com.gym.member_service.Dto.MemberFitDtos.Responses.MemberPrProgressResponseDto;
import com.gym.member_service.Dto.MemberFitDtos.Responses.PrSummaryResponseDto;
import com.gym.member_service.Dto.MemberFitDtos.Wrappers.MemberPrProgressWrapperDto;
import com.gym.member_service.Dto.MemberFitDtos.Wrappers.PrSummaryResponseWrapperDto;
import com.gym.member_service.Dto.NotificationDto.GenericResponse;
import com.gym.member_service.Exception.Exceptions.InvalidInputDateException;
import com.gym.member_service.Exception.Exceptions.InvalidPrUpdateException;
import com.gym.member_service.Model.Member;
import com.gym.member_service.Model.PrProgresses;
import com.gym.member_service.Model.PrSummary;
import com.gym.member_service.Repositories.PrProgressRepository;
import com.gym.member_service.Repositories.PrSummaryRepository;
import com.gym.member_service.Services.MemberServices.MemberManagementService;
import com.gym.member_service.Services.OtherService.SchedulerTaskService;
import com.gym.member_service.Utils.CustomJavaEvict;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberPrService {
    private final MemberManagementService managementService;
    private final PrSummaryRepository prSummaryRepository;
    private final PrProgressRepository prProgressRepository;
    private final CustomJavaEvict evict;
    private final SchedulerTaskService taskService;

    /**
     * Adds a new PR (personal record) entry for one or more workouts.
     *
     * <p>This method stores average weight, reps, and max values for
     * each workout on the given date. Used for tracking strength progression.</p>
     *
     * @param memberId   unique identifier of the member
     * @param requestDto list of PR request DTOs (for multiple workouts)
     * @return list of saved PR response DTOs
     */

    @Transactional
    public GenericResponse addANewPr(String memberId, List<PrProgressRequestDto> requestDto) {

        // Log the start of the process and the size of the batch
        log.info("Starting PR upsert for Member ID: {}. Received {} records.", memberId, requestDto.size());

        // --- 1. Validation and Setup ---

        // Fetch the member entity; throw exception if not found
        Member member =  managementService.cacheMemberDetails(memberId);

        // Validate for future dates and collect keys
        List<String> exerciseNames = new ArrayList<>();
        List<LocalDate> achievementDates = new ArrayList<>();

        requestDto.forEach(dto -> {
            if (dto.getAchievedDate().isAfter(LocalDate.now())) {
                log.warn("Invalid input date detected: {} for workout {}", dto.getAchievedDate(), dto.getWorkoutName());
                throw new InvalidInputDateException("Cannot add/update PRs for future dates: " + dto.getAchievedDate());
            }
            exerciseNames.add(dto.getWorkoutName());
            achievementDates.add(dto.getAchievedDate());
        });

        // Find all existing PRs matching the workout names and dates for this member
        log.debug("Batch lookup for existing PRs with {} unique exercise names and achievement dates.", exerciseNames.size());
        List<PrProgresses> existingPrs = prProgressRepository
                .findByMemberIdAndWorkoutNameInAndAchievedDateIn(memberId, exerciseNames, achievementDates);

        log.info("Found {} existing PR records that match the incoming batch.", existingPrs.size());

        // Map existing PRs for quick lookup
        Map<String, PrProgresses> existingMap = existingPrs.stream()
                .collect(Collectors.toMap(
                        pr -> pr.getWorkoutName() + "_" + pr.getAchievedDate(),
                        pr -> pr));

        List<PrProgresses> toSave = new ArrayList<>();
        int newCount = 0;
        int updateCount = 0;

        for (PrProgressRequestDto dto : requestDto) {
            String key = dto.getWorkoutName() + "_" + dto.getAchievedDate();

            // Check if an existing PR object exists for this key
            PrProgresses pr = existingMap.getOrDefault(key, new PrProgresses());

            // Determine if this is an update or a new insert for logging
            if (pr.getId() == null) {
                newCount++;
                log.trace("Creating new PR record for key: {}", key);
            } else {
                updateCount++;
                log.trace("Updating existing PR record (ID: {}) for key: {}", pr.getId(), key);
            }

            // Apply fields from DTO to the entity
            pr.setMember(member);
            pr.setWorkoutName(dto.getWorkoutName());
            pr.setAchievedDate(dto.getAchievedDate());
            pr.setWeight(dto.getWeight());
            pr.setRepetitions(dto.getRepetitions());

            toSave.add(pr);
        }

        log.info("Prepared to save: {} new records and {} updates.", newCount, updateCount);

        List<PrProgresses> saved = prProgressRepository.saveAll(toSave);
        taskService.computePr(member,LocalDate.now().minusMonths(1),LocalDate.now());
        log.info("Successfully persisted {} records to the database.", saved.size());
        log.info("Finished PR upsert for Member ID: {}", memberId);
        // custom evicting
        evict.evictMemberCachePattern("memberPrCache",memberId);
        evict.evictMemberCachePattern("membersMonthlyPrCache",memberId);
        String response = "Successfully added new bmi entry for "+member.getFirstName()+" "+member.getLastName();
        return new GenericResponse(response);
    }


    @Transactional
    public GenericResponse updateExistingPr(String memberId, String workoutName, UpdatePrRequestDto requestDto)
    {
        log.info("request reached service class to update existing pr for member {}",memberId);
        Member member =  managementService.cacheMemberDetails(memberId);
        log.info("Fetched member {} {} from db",member.getFirstName(), member.getLastName());
        PrProgresses pr = prProgressRepository.findByWorkoutAndDate(memberId,workoutName,requestDto.getArchivedDate())
                .orElseThrow(()-> {
                    log.warn("No existing workout found for member {} on date {} for workout {}",
                            memberId,requestDto.getArchivedDate(),workoutName);
                    return new InvalidPrUpdateException("No Pr found on "+ requestDto.getArchivedDate()+"for "+workoutName);
                });
        log.info("Fetched pr of {} on {} from database",pr.getWorkoutName(),pr.getAchievedDate());
        pr.setWeight(requestDto.getWeight());
        pr.setRepetitions(requestDto.getRepetitions());
        prProgressRepository.save(pr);
        log.info("Saved pr in the db for {} ",pr.getWorkoutName());
        taskService.computePr(member,LocalDate.now().minusMonths(1),LocalDate.now());
        // custom evicting
        evict.evictMemberCachePattern("memberPrCache",memberId);
        evict.evictMemberCachePattern("membersMonthlyPrCache",memberId);
        String response = "Successfully Update Pr for "+ member.getFirstName()+ " "+ member.getLastName();
        return new GenericResponse(response);
    }

    /**
     * Deletes a specific Personal Record (PR) entry for a given workout,
     * member, and date.
     *
     * <p>This method is used when a member has multiple workouts on the same
     * date, but only one workout entry (e.g., "Bench Press") needs to be
     * removed without affecting others.</p>
     *
     * <h2>Caching:</h2>
     * <ul>
     *   <li>Evicts related cache entries in {@code memberPrCache} to ensure consistency.</li>
     * </ul>
     *
     * @param memberId     unique identifier of the member
     * @param date         date of the workout entry
     * @param workoutName  name of the workout (e.g., "Squat", "Deadlift")
     * @return success message once deletion is completed
     */
    @Transactional
    public String deleteByWorkoutNameWIthMemberIdAndDate(String memberId, LocalDate date, String workoutName)
    {
        // custom method from repository responsible to return how many rows are effected
        // for this method

        int rowsEffected = prProgressRepository
                .deletePrByMemberIdWithDateAndName(memberId, date, workoutName);

        if (rowsEffected > 0) {  // if operation is successfully completed then
            Member member =  managementService.cacheMemberDetails(memberId);
            taskService.computePr(member,LocalDate.now().minusMonths(1),LocalDate.now());
            // custom evicting
            evict.evictMemberCachePattern("memberPrCache",memberId);
            evict.evictMemberCachePattern("membersMonthlyPrCache",memberId);
            return "Successfully deleted \n" +
                    rowsEffected + " no of entries of " + workoutName +
                    "\nof member id: " + memberId + "\n on: " + date;

        } else {  // if operation is failed due to any reason then
            return "Unable to found member by id: " + memberId +
                    "\nor \n the " + workoutName + " on: " + date;
        }
        /*
         * using if else to
         * return the desired responses
         * as per output for a valid response
         */
    }

    /**
     * Deletes all Personal Record (PR) entries for a given member
     * on a specific date.
     *
     * <p>If no records exist for the provided date, the method
     * performs no action. This operation is useful for correcting
     * incorrectly logged workout sessions.</p>
     *
     * <h2>Caching:</h2>
     * <ul>
     *   <li>Evicts related cache entries in {@code memberPrCache} to keep data consistent.</li>
     * </ul>
     *
     * @param memberId unique identifier of the member
     * @param date     date of the PR entries to delete
     * @return success message once deletion is completed
     */
    @Transactional
    public String deleteByIdAndDate(String memberId, LocalDate date) {
        // custom method in repository returns how many rows are effected for this method(db query operation)

        log.info("Request received to delete pr record for member on {}",date);
        int noOfRowsEffected = prProgressRepository.deleteByMemberIdAndDate(memberId, date);
        log.info("Rows effected by the query is  {}",noOfRowsEffected);
        if(noOfRowsEffected> 0 ){
            Member member =  managementService.cacheMemberDetails(memberId);
            taskService.computePr(member,LocalDate.now().minusMonths(1),LocalDate.now());
            log.info("Deleted and by the  query");
            // custom evicting
            evict.evictMemberCachePattern("memberPrCache",memberId);
            evict.evictMemberCachePattern("membersMonthlyPrCache",memberId);
        }
        return noOfRowsEffected > 0
                ? "Successfully deleted " + noOfRowsEffected + " entries of member of id: " + memberId + " on " + date
                : "Unable to found member by id: " + memberId + " or " + "date on: " + date;
        // if no member found or no data on specific date so output will be zero
        // if the effected rows > 0 then out put will be before ':' or else after the ':'
    }

    /**
     * Retrieves all Personal Record (PR) progress entries for the given member
     * within the specified number of past days.
     *
     * <p>This method queries the database for PR entries, aggregates them,
     * and returns them as a wrapped response. Results are useful for
     * displaying workout performance trends in graphs or reports.</p>
     *
     * <h2>Caching:</h2>
     * <ul>
     *   <li>Data may be cached (via Redis) to reduce repeated database calls.</li>
     * </ul>
     *
     * @param memberId unique identifier of the member
     * @param sortDirection     number of days of history to fetch (e.g., last 7, 30)
     * @return wrapper DTO containing list of PR progress records
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "memberPrCache",
            key = "#memberId + ':search:' + (#searchBy ?: '') + ':dir:' + #sortDirection + ':p:' + #pageNo + ':s:' + #pageSize + ':f:' + #from + ':t:' + #to")
    public MemberPrProgressWrapperDto getAllPrProgress
    (String memberId, int pageNo, int pageSize,
     String searchBy, String sortDirection, LocalDate from, LocalDate to) {
        log.info("Request received to get Paginated sorted date for member {}",memberId);
        Member member =  managementService.cacheMemberDetails(memberId);
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable page = PageRequest.of(pageNo,pageSize,Sort.by(direction, "achievedDate"));
        Page<PrProgresses> progresses = prProgressRepository
                .findForMemberBySearchDirectionWithDateRangeAndPage(member,searchBy,from,to,page);

        log.info("Fetched total {} no records for member {} in date range of {} -- {}",
                member.getFirstName()+" "+member.getLastName(), progresses.getSize(),from,to);

        return MemberPrProgressWrapperDto.builder()
                .responseDtoList(progresses.stream()
                        .map(pr-> MemberPrProgressResponseDto.builder()
                                .workoutName(pr.getWorkoutName())
                                .weight(pr.getWeight())
                                .repetitions(pr.getRepetitions())
                                .achievedDate(pr.getAchievedDate())
                                .build()).toList())
                .pageNo(progresses.getNumber())
                .pageSize(progresses.getSize())
                .totalElements(progresses.getTotalElements())
                .totalPages(progresses.getTotalPages())
                .lastPage(progresses.isLast())
                .build();
    }

    /**
     * Fetches all monthly PR summaries (personal records) for a given member.
     * <p>
     * Uses Redis caching to reduce DB load. Cache key is the memberId.
     * <p>
     * Data is wrapped in {@link PrSummaryResponseWrapperDto} to avoid
     * Redis serialization issues with plain lists.
     *
     * @param memberId ID of the member whose PR report is requested.
     * @return Wrapped DTO containing the list of PR monthly summaries.
     */
    @Cacheable(value = "membersMonthlyPrCache",
            key = "#memberId + ':search:' + (#searchBy ?: '') + ':dir:' + #sortDirection + ':p:' + #pageNo + ':s:' + #pageSize + ':f:' + #from + ':t:' + #to")
    @Transactional(readOnly = true)
    public PrSummaryResponseWrapperDto getPrReportByMonth(String memberId,int pageNo, int pageSize,
                                                          String searchBy,String sortDirection, LocalDate from, LocalDate to) {
        log.info("Request reached for member's pr summary for {} for page {} with size {}",
                memberId,pageNo,pageSize);

        Member member =  managementService.cacheMemberDetails(memberId);
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(
                new Sort.Order(direction, "year"), // Primary sort by year
                new Sort.Order(direction, "month") // Secondary sort by month
        );
        Pageable page = PageRequest.of(pageNo,pageSize,sort);

        Page<PrSummary> summaries = prSummaryRepository
                .findForMemberBySearchDirectionWithDateRangeAndPage(member,searchBy,from,to,page);
        log.info("Fetched {} no of data from db",summaries.getSize());
        return PrSummaryResponseWrapperDto.builder()
                .responseDtoList(summaries.stream().map(pr-> PrSummaryResponseDto.builder()
                        .monthValue(pr.getMonth())
                        .year(pr.getYear())
                        .workoutName(pr.getWorkoutName())
                        .avgWeight(pr.getAvgWeight())
                        .avgReps(pr.getAvgReps())
                        .maxWeight(pr.getMaxWeight())
                        .maxReps(pr.getMaxReps())
                        .entryCount(pr.getEntryCount())
                        .build()).toList())
                .pageNo(summaries.getNumber())
                .pageSize(summaries.getSize())
                .totalElements(summaries.getTotalElements())
                .totalPages(summaries.getTotalPages())
                .lastPage(summaries.isLast())
                .build();
    }



}
