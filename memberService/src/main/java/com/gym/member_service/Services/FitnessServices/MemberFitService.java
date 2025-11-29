package com.gym.member_service.Services.FitnessServices;

import com.gym.member_service.Controllers.MemberAllFitController;
import com.gym.member_service.Dto.MemberFitDtos.Requests.MemberWeighBmiEntryRequestDto;
import com.gym.member_service.Dto.MemberFitDtos.Responses.BmiSummaryResponseDto;
import com.gym.member_service.Dto.MemberFitDtos.Responses.BmiWeightInfoResponseDto;
import com.gym.member_service.Dto.MemberFitDtos.Wrappers.BmiSummaryResponseWrapperDto;
import com.gym.member_service.Dto.MemberFitDtos.Wrappers.MemberBmiResponseWrapperDto;
import com.gym.member_service.Dto.MemberFitDtos.Responses.MemberWeighBmiEntryResponseDto;
import com.gym.member_service.Dto.MemberFitDtos.Wrappers.PrSummaryResponseWrapperDto;
import com.gym.member_service.Exception.Exceptions.DuplicateUserFoundException;
import com.gym.member_service.Model.*;
import com.gym.member_service.Repositories.*;
import com.gym.member_service.Services.MemberServices.MemberManagementService;
import com.gym.member_service.Services.OtherService.SchedulerTaskService;
import com.gym.member_service.Utils.CustomJavaEvict;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * Service layer for managing member fitness data such as BMI entries,
 * weight progress, PR (personal record) tracking, and monthly summaries.
 *
 * <p>This service acts as the business logic layer between the controller
 * ({@link MemberAllFitController}) and the persistence layer
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
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberFitService {

    private final MemberManagementService managementService;
    private final MemberRepository memberRepository;
    private final WeightBmiEntryRepository weightBmiEntryRepository;
    private final BmiSummaryRepository bmiSummaryRepository;
    private final SchedulerTaskService taskService;
    private final CustomJavaEvict evict;
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
            @CacheEvict(value = "BmiWeight", key = "#memberId"),
            @CacheEvict(value = "MemberEntity", key = "#memberId")
    })
    public MemberWeighBmiEntryResponseDto addWeighBmiEntry(String memberId, MemberWeighBmiEntryRequestDto requestDto) {
        log.info("request reached service class to add bmi entry for member {}",memberId);
        Member member = managementService.cacheMemberDetails(memberId);
        log.debug("Found member with ID: {}", memberId);
        Optional<WeightBmiEntry> existingEntryOpt = weightBmiEntryRepository
                .findByMemberIdAndDate(memberId, requestDto.getDate());

        WeightBmiEntry bmiEntry;
        String operationType;
        if (existingEntryOpt.isPresent()) {
            bmiEntry = existingEntryOpt.get();
            bmiEntry.setBmi(requestDto.getBmi());
            bmiEntry.setMember(member);
            bmiEntry.setWeight(requestDto.getWeight());
            operationType = "UPDATE";
            log.info("DB OPERATION :: Existing BMI entry found for member {} on {}. Preparing to update.",
                    memberId, requestDto.getDate());
        } else {
            bmiEntry = WeightBmiEntry.builder()
                    .date(requestDto.getDate())
                    .member(member)
                    .bmi(requestDto.getBmi())
                    .weight(requestDto.getWeight())
                    .build();
            operationType = "CREATE";
            log.info("DB OPERATION :: No existing BMI entry found for member {} on {}. Preparing to create new entry.",
                    memberId, requestDto.getDate());
        }

        LocalDate start = requestDto.getDate().withDayOfMonth(1);
        log.info("calling schedular's logic to update summaries between range tof start day {} and end day {}",
                start,requestDto.getDate());
        taskService.computeWeightBmi(member,start,requestDto.getDate());
        if (!requestDto.getDate().isBefore(LocalDate.now())) {
            log.info("MEMBER UPDATE :: Date {} is today or future. Updating member {}'s current BMI to {}.",
                    requestDto.getDate(), memberId, requestDto.getBmi());
            member.setCurrentBmi(requestDto.getBmi());
            memberRepository.save(member);
            log.debug("MEMBER UPDATE :: Member entity saved successfully with new current BMI.");
        }

        WeightBmiEntry responseEntry = weightBmiEntryRepository.save(bmiEntry);
        evict.evictMemberCachePattern("memberBmiCache",memberId);
        evict.evictMemberCachePattern("membersMonthlyBmiCache",memberId);
        log.info("SUCCESS {} :: BMI entry (ID: {}) for member {} on {} was successfully persisted.",
                operationType, responseEntry.getId(), memberId, responseEntry.getDate());
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
     * @param pageNo and      number of page of history to fetch
     * @param pageSize how many data will be sent on every request
     * @return wrapper DTO containing list of BMI entries
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "memberBmiCache", key = "#memberId + ':pageNo:' + #pageNo + '::pageSize:' + #pageSize")
    public MemberBmiResponseWrapperDto getAllBmiEntry(String memberId, int pageNo, int pageSize) {
        Pageable page = PageRequest.of(pageNo,pageSize);
        Page<WeightBmiEntry> bmiEntries = weightBmiEntryRepository
                .findWeightBmiEntryByPages(memberId, page);

        List<MemberWeighBmiEntryResponseDto> bmiList = bmiEntries.stream()
                .map(entry -> MemberWeighBmiEntryResponseDto.builder()
                        .weight(entry.getWeight())
                        .bmi(entry.getBmi())
                        .date(entry.getDate())
                        .build())
                .toList();
        return MemberBmiResponseWrapperDto.builder()
                .bmiEntryResponseDtoList(bmiList)
                .lastPage(bmiEntries.isLast())
                .pageNo(bmiEntries.getNumber())
                .pageSize(bmiEntries.getSize())
                .totalElements(bmiEntries.getTotalElements())
                .totalPages(bmiEntries.getTotalPages())
                .build();
    }

    @Cacheable(value = "BmiWeight", key = "#memberId")
    public BmiWeightInfoResponseDto getBmiWeightInfo(String memberId) {
        LocalDate current = LocalDate.now();
        LocalDate comparisonDate = current.withDayOfMonth(1).minusDays(1);

        log.info("🗓️ Calculating BMI/Weight Info for Member: {}. Current Date: {}. Comparison Boundary: {}",
                memberId, current, comparisonDate);
        WeightBmiEntry latestEntry = weightBmiEntryRepository.findLatestDataByMonthAndId(memberId, current)
                .orElse(null);
        log.info("🔎 Latest Entry (up to {}): {}", current, latestEntry != null ? latestEntry.getDate() : "NULL");

        WeightBmiEntry oldEntry = weightBmiEntryRepository.findLatestDataByMonthAndId(memberId, comparisonDate)
                .orElse(null);
        log.info("🔎 Old Entry (up to {}): {}", comparisonDate, oldEntry != null ? oldEntry.getDate() : "NULL");


        // Case A: Data exists for both (calculate change)
        if(latestEntry != null && oldEntry != null) {
            log.info("✅ Case A: Found data for both periods. Calculating change.");
            // We must check if the two entries are actually different records.
            // If latestEntry == oldEntry, it means no new data was logged in the current month.
            if (latestEntry.equals(oldEntry)) {
                log.warn("⚠️ Latest Entry is the same as Old Entry. No change calculated (Result: 0.00).");
                return BmiWeightInfoResponseDto.builder()
                        .currentBmi(latestEntry.getBmi())
                        .currentBodyWeight(latestEntry.getWeight())
                        .changedBmiFromLastMonth(0.00)
                        .changedBodyWeightFromLastMonth(0.00)
                        .latestDate(latestEntry.getDate())
                        .oldDateTime(latestEntry.getDate())
                        .build();
            }
            return BmiWeightInfoResponseDto.builder()
                    .currentBmi(latestEntry.getBmi())
                    .currentBodyWeight(latestEntry.getWeight())
                    .changedBmiFromLastMonth(latestEntry.getBmi() - oldEntry.getBmi())
                    .changedBodyWeightFromLastMonth(latestEntry.getWeight() - oldEntry.getWeight())
                    .latestDate(latestEntry.getDate())
                    .oldDateTime(oldEntry.getDate())
                    .build();
            // Case B: Current data is missing, but historical data exists.
        } else if (latestEntry == null && oldEntry != null) {
            // NOTE: The original code contained a bug here (setting currentBodyWeight twice).
            // It has been corrected to use the historical data as the 'current' view, with 0.00 change.
            log.info("➡️ Case B: Current entry is missing. Returning historical data as current status (No change).");
            return BmiWeightInfoResponseDto.builder()
                    .currentBmi(oldEntry.getBmi())
                    .currentBodyWeight(oldEntry.getWeight())
                    .changedBmiFromLastMonth(0.00)
                    .changedBodyWeightFromLastMonth(0.00)
                    .latestDate(null)
                    .oldDateTime(oldEntry.getDate())
                    .build();

            // Case C: Only latest data exists (old data is null). This should only happen
            // if the member's first ever entry occurred after the comparisonDate.
        } else if (latestEntry != null) {
            log.info("⭐ Case C: Only latest entry found. Treating as initial record (No change).");
            return BmiWeightInfoResponseDto.builder()
                    .currentBodyWeight(latestEntry.getWeight())
                    .currentBmi(latestEntry.getBmi())
                    .changedBmiFromLastMonth(0.00)
                    .changedBodyWeightFromLastMonth(0.00)
                    .latestDate(latestEntry.getDate())
                    .oldDateTime(null)
                    .build();
        }

        // Case D: No data found at all.
        log.info("❌ Case D: No weight/BMI data found for member {}.", memberId);
        return null;
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

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "BmiWeight", key = "#memberId"),
            @CacheEvict(value = "MemberEntity", key = "#memberId")
    })
    public String deleteByDateAndId(String memberId, LocalDate date) {
        int deleted = weightBmiEntryRepository.deleteByMember_IdAndDate(memberId, date);

        Member member = managementService.cacheMemberDetails(memberId);
        taskService.computeWeightBmi(member,LocalDate.now().minusMonths(1),LocalDate.now());
        evict.evictMemberCachePattern("memberBmiCache",memberId);
        evict.evictMemberCachePattern("membersMonthlyBmiCache",memberId);
        return deleted > 0 ? "Successfully deleted a entry of member with id: " + memberId + " on: " + date
                : "Unable to find member with id: " + memberId + "or with date: " + date;
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
    @Cacheable(value = "membersMonthlyBmiCache", key = "#memberId + ':' + #pageNo + ':' + #pageSize")
    @Transactional(readOnly = true)
    public BmiSummaryResponseWrapperDto getBmiReportByMonth(String memberId, int pageNo, int pageSize) {
        log.info("request reached service class to get member's bmi summary from {} to {}",
                LocalDate.now(),LocalDate.now().minusMonths(pageSize));
        // Fetch raw BMI summaries for this member from DB
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        log.info("processing request to get member's summary for last {} months of year from {} to  {}",pageSize,
              LocalDate.now(),  LocalDate.now().minusYears(pageNo+1));
        Page<BmiSummary> summaries = bmiSummaryRepository.findByMemberId(memberId,pageable);
        return BmiSummaryResponseWrapperDto.builder()
                .summaryResponseDto(summaries.stream()
                        .map(bmiSummary -> BmiSummaryResponseDto.builder()
                                .maxBmi(bmiSummary.getMaxBmi())
                                .avgBmi(bmiSummary.getAvgBmi())
                                .minBmi(bmiSummary.getMinBmi())
                                .maxWeight(bmiSummary.getMaxWeight())
                                .avgWeight(bmiSummary.getAvgWeight())
                                .minWeight(bmiSummary.getMinWeight())
                                .entryCount(bmiSummary.getEntryCount())
                                .monthValue(bmiSummary.getMonth())
                                .year(bmiSummary.getYear())
                                .build()).toList())
                .build();
    }

}
