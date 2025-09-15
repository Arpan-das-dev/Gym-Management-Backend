package com.gym.member_service.Controllers;

import com.gym.member_service.Dto.MemberFitDtos.Requests.MemberWeighBmiEntryRequestDto;
import com.gym.member_service.Dto.MemberFitDtos.Requests.PrProgressRequestDto;
import com.gym.member_service.Dto.MemberFitDtos.Wrappers.BmiSummaryResponseWrapperDto;
import com.gym.member_service.Dto.MemberFitDtos.Wrappers.MemberBmiResponseWrapperDto;
import com.gym.member_service.Dto.MemberFitDtos.Responses.MemberWeighBmiEntryResponseDto;
import com.gym.member_service.Dto.MemberFitDtos.Responses.MemberPrProgressResponseDto;
import com.gym.member_service.Dto.MemberFitDtos.Wrappers.MemberPrProgressWrapperDto;
import com.gym.member_service.Services.FitnessServices.MemberFitService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.PastOrPresent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
/**
 * REST controller for managing Member Fitness data (BMI entries and PR progress).
 * <p>
 * Handles CRUD operations for BMI & PR records and also exposes monthly summary reports.
 * All business logic is delegated to {@link MemberFitService}.
 *
 * <p><b>Base URL:</b> configured via {@code member-service.Base_Url.Fit} in application.properties
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("${member-service.Base_Url.Fit}")
@Validated

public class MemberFitController {

    private final MemberFitService fitService;
    /**
     * Add a new BMI/weight entry for a given member.
     *
     * @param memberId   unique member identifier
     * @param requestDto request payload containing BMI & weight details
     * @return created entry details
     *
     * <p>Returns {@link HttpStatus#CREATED} if successfully inserted.</p>
     */
    @PostMapping("/weight-bmi-entry")
    ResponseEntity<MemberWeighBmiEntryResponseDto> addWeightBmi(@RequestParam String memberId,
                                                                @Valid @RequestBody MemberWeighBmiEntryRequestDto
                                                                        requestDto)
    {
        MemberWeighBmiEntryResponseDto responseDto = fitService.addWeighBmiEntry(memberId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }
    /**
     * Retrieve all BMI/weight entries for a member within the last given number of days.
     *
     * @param memberId member identifier
     * @param days     number of days to look back
     * @return list of BMI entries wrapped in response DTO
     *
     * <p>Returns {@link HttpStatus#ACCEPTED} when records are fetched successfully.</p>
     */
    @GetMapping("/weightBmi")
    ResponseEntity<MemberBmiResponseWrapperDto> getAllBmiListById(@RequestParam String memberId,
                                                                  @RequestParam int days) {
        MemberBmiResponseWrapperDto responseDtoList = fitService.getAllBmiEntry(memberId, days);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(responseDtoList);
    }
    /**
     * Delete a BMI/weight entry for a given member on a specific date.
     *
     * @param memberId member identifier
     * @param date     date for which BMI entry should be deleted (must not be in the future)
     * @return confirmation message
     *
     * <p>Returns {@link HttpStatus#OK} if deletion was successful.</p>
     */
    @DeleteMapping("/weightBmi")
    ResponseEntity<String> deleteByIdDate(@RequestParam String memberId,
                                          @RequestParam
                                          @FutureOrPresent(message = "Can not delete a pr from future")
                                          LocalDate date)
    {
       String response = fitService.deleteByDateAndId(memberId,date);
       return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    /**
     * Add one or more new PR (Personal Record) entries for a given member.
     *
     * @param memberId   member identifier
     * @param requestDto list of PR request payloads
     * @return list of created PR entries
     *
     * <p>Returns {@link HttpStatus#CREATED} if PR entries were added successfully.</p>
     */
    @PostMapping("/addPr")
    ResponseEntity<List<MemberPrProgressResponseDto>> addNewPr(@RequestParam String memberId,
                                                               @Valid @RequestBody List<PrProgressRequestDto>
                                                                       requestDto)
    {
        List<MemberPrProgressResponseDto> responseDtoList = fitService.addANewPr(memberId,requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDtoList);
    }
    /**
     * Retrieve all PR progress entries for a member within the last given number of days.
     *
     * @param memberId member identifier
     * @param days     number of days to look back
     * @return wrapper DTO containing list of PR progress entries
     *
     * <p>Returns {@link HttpStatus#ACCEPTED} when records are fetched successfully.</p>
     */
    @GetMapping("/pr")
    ResponseEntity<MemberPrProgressWrapperDto> getAllPrProgress(@RequestParam String memberId,
                                                                @RequestParam int days)
    {
        MemberPrProgressWrapperDto response = fitService.getAllPrProgress(memberId,days);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
    /**
     * Delete all PR progress entries for a given member on a specific date.
     *
     * @param memberId member identifier
     * @param date     date for which PR entries should be deleted (must not be in the future)
     * @return confirmation message
     *
     * <p>Returns {@link HttpStatus#OK} if deletion was successful.</p>
     */
    @DeleteMapping("/pr")
    ResponseEntity<String> deletePrByIdDate(@RequestParam String memberId,
                                            @RequestParam
                                            @PastOrPresent(message = "Can not delete a pr from future")
                                            LocalDate date)
    {
        String response = fitService.deleteByIdAndDate(memberId,date);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    /**
     * Delete a PR entry for a specific workout of a member on a given date.
     *
     * @param memberId    member identifier
     * @param date        date for which PR entry should be deleted
     * @param workoutName workout name to identify which PR to delete
     * @return confirmation message
     *
     * <p>Returns {@link HttpStatus#OK} if deletion was successful.</p>
     */
    @DeleteMapping("/deletePr/{workoutName}")
    ResponseEntity<String> deleteByWorkoutName(@RequestParam String memberId,
                                               @RequestParam
                                               @PastOrPresent(message = "Can not delete a pr from future")
                                               LocalDate date,
                                               @PathVariable String workoutName)
    {
        String response = fitService.deleteByWorkoutNameWIthMemberIdAndDate(memberId,date,workoutName);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    /**
     * Retrieve pre-aggregated monthly BMI summary for a given member.
     *
     * @param memberId member identifier
     * @return monthly BMI summary report
     *
     * <p>Returns {@link HttpStatus#OK} along with cached data (via Redis) if available.</p>
     */
    @GetMapping("/bmiSummary")
    ResponseEntity<BmiSummaryResponseWrapperDto> getMonthlyBmiReport(@RequestParam String memberId){
        BmiSummaryResponseWrapperDto response = fitService.getBmiReportByMonth(memberId);
        return  ResponseEntity.status(HttpStatus.OK).body(response);
    }
    /**
     * Retrieve pre-aggregated monthly PR summary for a given member.
     *
     * @param memberId member identifier
     * @return monthly PR summary report
     *
     * <p>Returns {@link HttpStatus#OK} along with cached data (via Redis) if available.</p>
     */
    @GetMapping("/prSummary")
    ResponseEntity<?> getMonthlyPrReport(@RequestParam String memberId){
        Object response = fitService.getPrReportByMonth(memberId);
        return  ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
