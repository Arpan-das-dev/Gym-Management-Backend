package com.gym.member_service.Services.FitnessServices;

import com.gym.member_service.Controllers.MemberFitController;
import com.gym.member_service.Dto.MemberFitDtos.Requests.MemberWeighBmiEntryRequestDto;
import com.gym.member_service.Dto.MemberFitDtos.Requests.PrProgressRequestDto;
import com.gym.member_service.Dto.MemberFitDtos.Responses.BmiSummaryResponseDto;
import com.gym.member_service.Dto.MemberFitDtos.Responses.PrSummaryResponseDto;
import com.gym.member_service.Dto.MemberFitDtos.Wrappers.BmiSummaryResponseWrapperDto;
import com.gym.member_service.Dto.MemberFitDtos.Wrappers.MemberBmiResponseWrapperDto;
import com.gym.member_service.Dto.MemberFitDtos.Responses.MemberWeighBmiEntryResponseDto;
import com.gym.member_service.Dto.MemberFitDtos.Responses.MemberPrProgressResponseDto;
import com.gym.member_service.Dto.MemberFitDtos.Wrappers.MemberPrProgressWrapperDto;
import com.gym.member_service.Dto.MemberFitDtos.Wrappers.PrSummaryResponseWrapperDto;
import com.gym.member_service.Exception.Exceptions.DuplicateUserFoundException;
import com.gym.member_service.Exception.Exceptions.InvalidInputDateException;
import com.gym.member_service.Exception.Exceptions.UserNotFoundException;
import com.gym.member_service.Model.*;
import com.gym.member_service.Repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer for managing member fitness data such as BMI entries,
 * weight progress, PR (personal record) tracking, and monthly summaries.
 *
 * <p>This service acts as the business logic layer between the controller
 * ({@link MemberFitController}) and the persistence layer
 * (repositories). It ensures validation, transactional safety, and
 * cache consistency for all fitness-related operations.
 *
 * <h2>Responsibilities:</h2>
 * <ul>
 *   <li>Manage Weight/BMI entries for members (add, fetch, delete).</li>
 *   <li>Handle PR (personal record) progress tracking (add, fetch, delete).</li>
 *   <li>Provide monthly summaries (BMI & PR) using pre-aggregated tables.</li>
 *   <li>Maintain cache consistency across operations using Redis.</li>
 *   <li>Ensure atomic operations with Spring’s {@code @Transactional} where required.</li>
 * </ul>
 *
 * <h2>Caching Strategy:</h2>
 * <ul>
 *   <li>{@code memberCache} – cache for individual member details.</li>
 *   <li>{@code memberListCache} – cache for global member list.</li>
 *   <li>{@code memberBmiCache} – cache for individual BMI entry history.</li>
 *   <li>{@code memberPrCache} – cache for PR progress per member.</li>
 *   <li>{@code member'sMonthlyBmrCache} – cache for monthly BMI summaries.</li>
 *   <li>{@code member'sMonthlyPrCache} – cache for monthly PR summaries.</li>
 * </ul>
 *
 * <h2>Transactional Strategy:</h2>
 * <ul>
 *   <li>Write operations use {@code @Transactional} to guarantee
 *       rollback on failure (atomic updates).</li>
 *   <li>Read operations use {@code @Transactional(readOnly = true)}
 *       to optimize performance.</li>
 * </ul>
 *
 * <p><b>Note:</b> Wrapper DTOs (e.g. {@link BmiSummaryResponseWrapperDto},
 * {@link PrSummaryResponseWrapperDto}) are used to avoid Redis
 * serialization issues with plain lists.</p>
 */
@Service
@RequiredArgsConstructor
public class MemberFitService {

    private final MemberRepository memberRepository;
    private final WeightBmiEntryRepository weightBmiEntryRepository;
    private final PrProgressRepository prProgressRepository;
    private final BmiSummaryRepository bmiSummaryRepository;
    private final PrSummaryRepository prSummaryRepository;

    /**
     * Adds a new weight and BMI entry for the specified member.
     *
     * <p>This method validates and persists the member's daily
     * weight and BMI data. If the member already has an entry for
     * the given date, it will be replaced or updated based on
     * repository rules.</p>
     *
     * <h2>Caching:</h2>
     * <ul>
     *   <li>Evicts related entries in {@code memberBmiCache} to keep data consistent.</li>
     * </ul>
     *
     * @param memberId   unique identifier of the member
     * @param requestDto request payload containing weight, BMI, and date
     * @return a response DTO containing the saved entry details
     * @throws DuplicateUserFoundException if the entry already exists (depending on rules)
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "memberListCache", key = "'All'"),
            @CacheEvict(value = "memberCache", key = "#memberId"),
            @CacheEvict(value = "memberBmiCache", key = "#memberId")
    })
    public MemberWeighBmiEntryResponseDto addWeighBmiEntry(String memberId, MemberWeighBmiEntryRequestDto requestDto) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UserNotFoundException("Member with id " + memberId + " not found"));
        Optional<WeightBmiEntry> existingEntryOpt = weightBmiEntryRepository
                .findByMemberIdAndDate(memberId, requestDto.getDate());

        WeightBmiEntry bmiEntry;
        if (existingEntryOpt.isPresent()) {
            bmiEntry = existingEntryOpt.get();
            bmiEntry.setBmi(requestDto.getBmi());
            bmiEntry.setMember(member);
            bmiEntry.setWeight(requestDto.getWeight());
        } else {
            bmiEntry = WeightBmiEntry.builder()
                    .date(requestDto.getDate())
                    .member(member)
                    .bmi(requestDto.getBmi())
                    .weight(requestDto.getWeight())
                    .build();
        }

        if (!requestDto.getDate().isBefore(LocalDate.now())) {
            member.setCurrentBmi(requestDto.getBmi());
            memberRepository.save(member);
        }

        WeightBmiEntry responseEntry = weightBmiEntryRepository.save(bmiEntry);
        return MemberWeighBmiEntryResponseDto.builder()
                .date(responseEntry.getDate())
                .bmi(responseEntry.getBmi())
                .weight(responseEntry.getWeight())
                .build();
    }
    /**
     * Retrieves all BMI entries for a member within the last {@code days}.
     *
     * <p>Data is cached in Redis to avoid repeated DB lookups.
     * If cached data exists, it is returned directly without hitting
     * the database.</p>
     *
     * <h2>Use Case:</h2>
     * <ul>
     *   <li>Show BMI trends in graphs (daily/weekly/monthly).</li>
     *   <li>Support analytics dashboards.</li>
     * </ul>
     *
     * @param memberId member identifier
     * @param days     number of days of history to fetch
     * @return wrapper DTO containing list of BMI entries
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "memberBmiCache", key = "#memberId")
    public MemberBmiResponseWrapperDto getAllBmiEntry(String memberId, int days) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.minusDays(days - 1);
        List<WeightBmiEntry> bmiEntries = weightBmiEntryRepository
                .findMemberByDateRange(memberId, endDate,startDate);

        List<MemberWeighBmiEntryResponseDto> bmiList = bmiEntries.stream()
                .map(entry -> MemberWeighBmiEntryResponseDto.builder()
                        .weight(entry.getWeight())
                        .bmi(entry.getBmi())
                        .date(entry.getDate())
                        .build())
                .toList();
        return MemberBmiResponseWrapperDto.builder()
                .bmiEntryResponseDtoList(bmiList)
                .build();
    }
    /**
     * Deletes a member's BMI entry for a specific date.
     *
     * <p>If no entry exists for the provided date, no action is taken.</p>
     *
     * <h2>Caching:</h2>
     * <ul>
     *   <li>Evicts related entries in {@code memberBmiCache}.</li>
     * </ul>
     *
     * @param memberId unique identifier of the member
     * @param date     date of the entry to delete
     * @return success message once deleted
     */
    @Caching(evict = {
            @CacheEvict(value = "memberBmiCache", key = "#memberId"),
            @CacheEvict(value = "memberListCache", key = "'All'"),
            @CacheEvict(value = "memberCache", key = "#memberId")
    })
    @Transactional
    public String deleteByDateAndId(String memberId, LocalDate date) {
        int deleted = weightBmiEntryRepository.deleteByMember_IdAndDate(memberId, date);

        return deleted > 0 ? "Successfully deleted a entry of member with id: " + memberId + " on: " + date
                : "Unable to find member with id: " + memberId + "or with date: " + date;
    }
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
    @CacheEvict(value = "memberPrCache", key = "#memberId")
    public List<MemberPrProgressResponseDto> addANewPr(String memberId, List<PrProgressRequestDto> requestDto) {
        // Fetch the member entity; throw exception if not found
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UserNotFoundException("Member with id " + memberId + " not found"));

        requestDto.forEach(dto -> {
            if (dto.getAchievedDate().isBefore(LocalDate.now())) {
                throw new InvalidInputDateException("Cannot add/update PRs for past dates: " + dto.getAchievedDate());
            }
        });
        List<String> exerciseNames = requestDto.stream()
                .map(PrProgressRequestDto::getWorkoutName)
                .toList();
        List<LocalDate> achievementDates = requestDto.stream()
                .map(PrProgressRequestDto::getAchievedDate)
                .toList();

        List<PrProgresses> existingPrs = prProgressRepository
                .findByMemberIdAndWorkoutNameInAndAchievedDateIn(memberId, exerciseNames, achievementDates);

        Map<String, PrProgresses> existingMap = existingPrs.stream()
                .collect(Collectors.toMap(
                        pr -> pr.getWorkoutName() + "_" + pr.getAchievedDate(),
                        pr -> pr));
        // 6. Build or update PRs
        List<PrProgresses> toSave = new ArrayList<>();

        for (PrProgressRequestDto dto : requestDto) {
            String key = dto.getWorkoutName() + "_" + dto.getAchievedDate();

            PrProgresses pr = existingMap.getOrDefault(key,
                    new PrProgresses());

            pr.setMember(member);
            pr.setWorkoutName(dto.getWorkoutName());
            pr.setAchievedDate(dto.getAchievedDate());
            pr.setWeight(dto.getWeight());
            pr.setRepetitions(dto.getRepetitions());

            toSave.add(pr);
        }
        // 7. Save in batch
        List<PrProgresses> saved = prProgressRepository.saveAll(toSave);
        // 8. Build response
        return saved.stream()
                .map(pr -> {
                    MemberPrProgressResponseDto dto = new MemberPrProgressResponseDto();
                    dto.setWorkoutName(pr.getWorkoutName());
                    dto.setAchievedDate(pr.getAchievedDate());
                    dto.setWeight(pr.getWeight());
                    dto.setRepetitions(pr.getRepetitions());
                    return dto;
                })
                .toList();
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
     * @param days     number of days of history to fetch (e.g., last 7, 30)
     * @return wrapper DTO containing list of PR progress records
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "memberPrCache", key = "#memberId")
    public MemberPrProgressWrapperDto getAllPrProgress(String memberId, int days) {
        LocalDate startDate = LocalDate.now(); //set the starting date as current date
        LocalDate endDate = startDate.minusDays(days-1); // set the end date as desired
        List<PrProgresses> prProgresses = prProgressRepository // a custom method defined in repository
                .findByMemberIdAndDateRange(memberId, endDate, startDate);
        // using stream and builder to return the required data
        return MemberPrProgressWrapperDto.builder()
                .responseDtoList(prProgresses.stream().map(pr -> MemberPrProgressResponseDto.builder()
                        .workoutName(pr.getWorkoutName())
                        .weight(pr.getWeight())
                        .repetitions(pr.getRepetitions())
                        .achievedDate(pr.getAchievedDate())
                        .build()).toList())
                .build();
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
    @CacheEvict(value = "memberPrCache", key = "#memberId")
    public String deleteByIdAndDate(String memberId, LocalDate date) {
        // custom method in repository returns how many rows are effected for this method(db query operation)
        int noOfRowsEffected = prProgressRepository.deleteByMemberIdAndDate(memberId, date);
        return noOfRowsEffected > 0
                ? "Successfully deleted " + noOfRowsEffected + " entries of member of id: " + memberId + " on " + date
                : "Unable to found member by id: " + memberId + " or " + "date on: " + date;
        // if no member found or no data on specific date so output will be zero
        // if the effected rows > 0 then out put will be before ':' or else after the ':'
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
    @CacheEvict(value = "memberPrCache", key = "#memberId")
    public String deleteByWorkoutNameWIthMemberIdAndDate(String memberId, LocalDate date, String workoutName)
    {
        // custom method from repository responsible to return how many rows are effected
        // for this method
        int rowsEffected = prProgressRepository
                .deletePrByMemberIdWithDateAndName(memberId, date, workoutName);

        if (rowsEffected > 0) {  // if operation is successfully completed then
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
     * Fetches all monthly BMI summaries for a given member.
     * <p>
     * Uses Redis caching to avoid hitting the DB repeatedly for the same member’s report.
     * The cache key is the memberId.
     * <p>
     * Data is wrapped in {@link BmiSummaryResponseWrapperDto} to avoid
     * Redis serialization issues with plain lists.
     *
     * @param memberId ID of the member whose BMI report is requested.
     * @return Wrapped DTO containing the list of BMI monthly summaries.
     */
    @Cacheable(value = "member'sMonthlyBmrCache",key = "#memberId")
    @Transactional(readOnly = true)
    public BmiSummaryResponseWrapperDto getBmiReportByMonth(String memberId) {
        // Fetch raw BMI summaries for this member from DB
       List<BmiSummary> summary = bmiSummaryRepository.findByMemberId(memberId);
        // Map entity -> DTO
        List<BmiSummaryResponseDto> responseDto = summary.stream()
                .map(response-> BmiSummaryResponseDto.builder()
                        .avgBmi(response.getAvgBmi())
                        .avgWeight(response.getAvgWeight())
                        .maxBmi(response.getMaxBmi())
                        .minBmi(response.getMinBmi())
                        .entryCount(response.getEntryCount())
                        .build())
                .collect(Collectors.toList());
        // Wrap the list to fix serialization issues & standardize response format
        return BmiSummaryResponseWrapperDto.builder()
               .summaryResponseDto(responseDto)
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
    @Cacheable(value = "member'sMonthlyPrCache", key = "#memberId")
    @Transactional(readOnly = true)
    public PrSummaryResponseWrapperDto getPrReportByMonth(String memberId) {
        // Fetch raw PR summaries for this member from DB
        List<PrSummary> summaries = prSummaryRepository.findByMemberId(memberId);
        // Map entity -> DTO
        List<PrSummaryResponseDto> responseDtoList = summaries.stream()
                .map(response-> PrSummaryResponseDto.builder()
                .workoutName(response.getWorkoutName())
                .avgWeight(response.getAvgWeight())
                .avgReps(response.getAvgReps())
                .maxReps(response.getMaxReps())
                .maxWeight(response.getMaxWeight())
                .entryCount(response.getEntryCount())
                .build())
                .collect(Collectors.toList());
        // Wrap the list to fix serialization issues & standardize response format
        return PrSummaryResponseWrapperDto.builder()
                .responseDtoList(responseDtoList)
                .build();
    }

}
/*
 * ============================================================================
 *  MemberFitService
 *  ---------------------------------------------------------------------------
 *  This service acts as the business logic layer for managing:
 *   - Member BMI & weight entries
 *   - Personal Record (PR) progress
 *   - Monthly summary reports for BMI and PR
 *
 *  Key Features:
 *   • Uses Spring Caching (backed by Redis) to reduce DB load.
 *   • Employs wrapper DTOs to avoid Redis serialization issues.
 *   • Ensures transactional consistency across DB operations.
 *   • Provides fine-grained cache eviction for member-specific updates.
 *
 *  Design Notes:
 *   • Caching is short-lived (6h) to balance performance & freshness.
 *   • Wrapper DTOs (instead of raw lists) standardize responses
 *     and future-proof serialization.
 *
 *  Future Enhancements:
 *   • Add pagination for large historical queries.
 *   • Introduce analytics aggregation (e.g., trends, streaks).
 *   • Integrate with notification service (alerts on PR milestones).
 *
 * ============================================================================
 */