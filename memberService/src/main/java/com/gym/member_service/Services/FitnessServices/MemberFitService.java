package com.gym.member_service.Services.FitnessServices;

import com.gym.member_service.Dto.MemberFitDtos.Requests.MemberWeighBmiEntryRequestDto;
import com.gym.member_service.Dto.MemberFitDtos.Requests.PrProgressRequestDto;
import com.gym.member_service.Dto.MemberFitDtos.Wrappers.MemberBmiResponseWrapperDto;
import com.gym.member_service.Dto.MemberFitDtos.Responses.MemberWeighBmiEntryResponseDto;
import com.gym.member_service.Dto.MemberFitDtos.Responses.MemberPrProgressResponseDto;
import com.gym.member_service.Dto.MemberFitDtos.Wrappers.MemberPrProgressWrapperDto;
import com.gym.member_service.Exception.Exceptions.InvalidInputDateException;
import com.gym.member_service.Exception.Exceptions.UserNotFoundException;
import com.gym.member_service.Model.Member;
import com.gym.member_service.Model.PrProgresses;
import com.gym.member_service.Model.WeightBmiEntry;
import com.gym.member_service.Repositories.MemberRepository;
import com.gym.member_service.Repositories.PrProgressRepository;
import com.gym.member_service.Repositories.WeightBmiEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberFitService {

    private final MemberRepository memberRepository;
    private final WeightBmiEntryRepository weightBmiEntryRepository;
    private final PrProgressRepository prProgressRepository;

    /*
     * Adds or updates a Weight & BMI entry for a specific member.
     *
     * Caching Strategy:
     * - Evicts the cached list of all members ("memberListCache")
     * because a new weight/BMI entry may affect aggregate stats.
     * - Evicts the cache entry for the specific member ("memberCache")
     * to ensure the next fetch is up-to-date.
     *
     * Transactional:
     * - Ensures that both updating the Member entity and saving the WeightBmiEntry
     * happen atomically. If any error occurs, changes are rolled back.
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "memberListCache", key = "'All'"),
            @CacheEvict(value = "memberCache", key = "#memberId"),
            @CacheEvict(value = "memberBmiCache", key = "#memberId")
    })
    public MemberWeighBmiEntryResponseDto addWeighBmiEntry(String memberId, MemberWeighBmiEntryRequestDto requestDto) {
        // Fetch the member entity; throw exception if not found
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UserNotFoundException("Member with id " + memberId + " not found"));
        // Check if an entry for the same date already exists (idempotent behavior)
        Optional<WeightBmiEntry> existingEntryOpt = weightBmiEntryRepository
                .findByMemberIdAndDate(memberId, requestDto.getDate());

        WeightBmiEntry bmiEntry;
        // Update existing entry
        if (existingEntryOpt.isPresent()) {
            bmiEntry = existingEntryOpt.get();
            bmiEntry.setBmi(requestDto.getBmi());
            bmiEntry.setMember(member);
            bmiEntry.setWeight(requestDto.getWeight());
            // Create new entry
        } else {
            bmiEntry = WeightBmiEntry.builder()
                    .date(requestDto.getDate())
                    .member(member)
                    .bmi(requestDto.getBmi())
                    .weight(requestDto.getWeight())
                    .build();
        }
        // Update member's current BMI if the entry is for today or future
        if (!requestDto.getDate().isBefore(LocalDate.now())) {
            member.setCurrentBmi(requestDto.getBmi());
            memberRepository.save(member);
        }
        // Persist the weight/BMI entry
        WeightBmiEntry responseEntry = weightBmiEntryRepository.save(bmiEntry);
        return MemberWeighBmiEntryResponseDto.builder() // Build and return the response DTO
                .date(responseEntry.getDate())
                .bmi(responseEntry.getBmi())
                .weight(responseEntry.getWeight())
                .build();
    }

    /*
     *  this method delivers a list of bmi and weight data
     *  for a verified member with a valid id
     *  and after successfully retrieving data from the database
     *  it stores the data in the cache till it get refreshed
     *  we here instead of using List<Thing> we wrapped it in a class
     *  because at this time(19:00 IST) i am facing a redis issue
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "memberBmiCache", key = "#memberId")
    public MemberBmiResponseWrapperDto getAllBmiEntry(String memberId, int days) {
        LocalDate startDate = LocalDate.now(); // set the start data as current day
        LocalDate endDate = startDate.minusDays(days - 1); // set the end date as desired
        List<WeightBmiEntry> bmiEntries = weightBmiEntryRepository
                .findMemberByDateRange(memberId, startDate, endDate); // custom method(definition) in repository
        // used stream and map with to list to convert data into desired data
        List<MemberWeighBmiEntryResponseDto> bmiList = bmiEntries.stream()
                .map(entry -> MemberWeighBmiEntryResponseDto.builder()
                        .weight(entry.getWeight())
                        .bmi(entry.getBmi())
                        .date(entry.getDate())
                        .build())
                .toList();
        return MemberBmiResponseWrapperDto.builder()
                .bmiEntryResponseDtoList(bmiList)
                .build(); // using builder patter and returning the method;
    }

    /*
     * This method is responsible for deleting a WEIGHT,BMI entries from
     * for a specific member (as per provided id) on the date
     * and also refresh the data in the cache to keep it fresh and updated
     * Transactional:
     * Ensures that both updating the Member entity and saving the WeightBmiEntry
     * happen atomically. If any error occurs, changes are rolled back.
     */
    @Caching(evict = {
            @CacheEvict(value = "memberBmiCache", key = "#memberId"),
            @CacheEvict(value = "memberListCache", key = "'All'"),
            @CacheEvict(value = "memberCache", key = "#memberId")
    })
    @Transactional
    public String deleteByDateAndId(String memberId, LocalDate date) {
        // custom method from repository to delete the data from db
        int deleted = weightBmiEntryRepository.deleteByMember_IdAndDate(memberId, date);
        // using a ternary operator to show the result
        // if no user found then the effected rows(here: deleted) will be zero
        return deleted > 0 ? "Successfully deleted a entry of member with id: " + memberId + " on: " + date
                : "Unable to find member with id: " + memberId + "or with date: " + date;
        // if the effected rows > 0 then out put will be before ':' or else after the ':'
    }

    /*
     * This method add a new pr in the db
     * with the work-out name and repetitions
     * it uses a custom method(definition provided in repository) to do so
     * and this data will be cached in a new cache instead of using @CachePut we
     * are using @CacheEvict due redis serialization/deserialization errors
     * Transactional:
     * to ensure that if any error occurs then changes are rolled back.
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

    /*
     * This method is responsible to provide all prs
     * in list format
     * it fetched data from database based on the range(cause less load on db)
     * using a custom method(from repository) to do so
     * it also uses a cache to store data for some time
     * and also uses a redis custom configuration
     * Transactional is set to read only
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "memberPrCache", key = "#memberId")
    public MemberPrProgressWrapperDto getAllPrProgress(String memberId, int days) {
        LocalDate startDate = LocalDate.now(); //set the starting date as current date
        LocalDate endDate = startDate.minusDays(days-1); // set the end date as desired
        List<PrProgresses> prProgresses = prProgressRepository // a custom method defined in repository
                .findByMemberIdAndDateRange(memberId, startDate, endDate);
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

    /*
     * This method is responsible if any user want to delete all pr from db on
     * certain date
     * it uses a custom method(defined in repository) to do so
     * after deleting it also updates the cache to keep it fresh
     * Transactional is set to ensure that:
     * if any error occurs all changes will be rolled back
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

    /*
     * This method is responsible to delete a specific pr from db
     * with a specific name on a certain date
     * a custom method (defined in repository) do so
     * after deleted it evicts the data from the cache
     * Transactional is set to ensure that:
     * if any problem occurs the changes will be rolled back
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

}