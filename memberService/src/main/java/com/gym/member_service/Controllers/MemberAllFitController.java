package com.gym.member_service.Controllers;

import com.gym.member_service.Dto.MemberFitDtos.Requests.MemberWeighBmiEntryRequestDto;
import com.gym.member_service.Dto.MemberFitDtos.Requests.PrProgressRequestDto;
import com.gym.member_service.Dto.MemberFitDtos.Requests.UpdatePrRequestDto;
import com.gym.member_service.Dto.MemberFitDtos.Wrappers.BmiSummaryResponseWrapperDto;
import com.gym.member_service.Dto.MemberFitDtos.Wrappers.MemberBmiResponseWrapperDto;
import com.gym.member_service.Dto.MemberFitDtos.Responses.MemberWeighBmiEntryResponseDto;
import com.gym.member_service.Dto.MemberFitDtos.Wrappers.MemberPrProgressWrapperDto;
import com.gym.member_service.Dto.NotificationDto.GenericResponse;
import com.gym.member_service.Services.FitnessServices.MemberFitService;
import com.gym.member_service.Services.FitnessServices.MemberPrService;
import com.gym.member_service.Utils.LogExecutionTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
/**
 * REST controller for managing Member Fitness data (BMI entries and PR progress).
 * <p>
 * Handles CRUD operations for BMI & PR records and also exposes monthly summary reports.
 * All business logic is delegated to {@link MemberFitService}.
 *
 * <p><b>Base URL:</b> configured via {@code member-service.Base_Url.Fit} in application.properties
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("${member-service.Base_Url.Fit}")
@Validated

public class MemberAllFitController {

    private final MemberFitService fitService;
    private final MemberPrService prService;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /**
     * Add a new BMI/weight entry for a given member.
     *
     * @param memberId   unique member identifier
     * @param requestDto request payload containing BMI & weight details
     * @return created entry details
     *
     * <p>Returns {@link HttpStatus#CREATED} if successfully inserted.</p>
     */
    @LogExecutionTime
    @PostMapping("/weight-bmi-entry")
    ResponseEntity<GenericResponse> addWeightBmi(
            @RequestParam @NotBlank(message = "member id is required to add new entries") String memberId,
                                                 @RequestBody MemberWeighBmiEntryRequestDto
                                                                        requestDto)
    {
        log.info("⌛⌛ {} request received to add new bmi entry for member {} on {}",
                LocalDateTime.now().format(formatter),memberId,requestDto.getDate());
        MemberWeighBmiEntryResponseDto response = fitService.addWeighBmiEntry(memberId, requestDto);
        log.info("successfully updated member's bmi entry of weight {} with bmi {}"
                , response.getWeight(),response.getBmi());
        String responseDto = "Successfully added new bmi entry";
        return ResponseEntity.status(HttpStatus.CREATED).body(new GenericResponse(responseDto));
    }
    /**
     * Retrieve all BMI/weight entries for a member within the last given number of days.
     *
     * @param memberId member identifier
     * @param pageNo and
     * @param pageSize  of days to look back
     * @return list of BMI entries wrapped in response DTO
     *
     * <p>Returns {@link HttpStatus#ACCEPTED} when records are fetched successfully.</p>
     */
    @LogExecutionTime
    @GetMapping("/get/{pageNo}/WeightBmiEntries/{pageSize}")
    ResponseEntity<MemberBmiResponseWrapperDto> getAllBmiListById(
            @RequestParam @NotBlank(message = "please verify your identity before add any entries") String memberId,
            @PathVariable @PositiveOrZero(message = "Can not proceed negative page numbers please try again") int pageNo,
            @PathVariable @PositiveOrZero(message = "Can not proceed negative page sizes please try again" ) int pageSize)
    {
        log.info("request received on ⌛⌛ {} to get bmi entries for member {} for last {}th month for {} days",
                LocalDateTime.now().format(formatter), memberId,pageNo, pageSize);
        MemberBmiResponseWrapperDto responseDtoList = fitService.getAllBmiEntry(memberId, pageNo,pageSize);
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
    @LogExecutionTime
    @DeleteMapping("/deleteWeightBmi")
    ResponseEntity<GenericResponse> deleteBmiByIdDate(
            @RequestParam @NotBlank(message = "please verify your identity to delete any entries") String memberId,
            @RequestParam @PastOrPresent(message = "Can not delete a bmi entry from future") LocalDate date) {
        log.info("⌛⌛ {} request received to to delete bmi entries for member {} for date {}",
                LocalDateTime.now().format(formatter),memberId,date);
        String response = fitService.deleteByDateAndId(memberId, date);
        return ResponseEntity.status(HttpStatus.OK).body(new GenericResponse(response));
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
    @LogExecutionTime
    @PostMapping("/addPr")
    ResponseEntity<GenericResponse> addNewPr(@RequestParam String memberId,
                                             @Valid @RequestBody List<PrProgressRequestDto> requestDto)
    {
        log.info("⌛⌛ {} request received to add pr entries for member {}",
                LocalDateTime.now().format(formatter),memberId);
        GenericResponse response = prService.addANewPr(memberId,requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    /**
     * Retrieve all PR progress entries for a member within the last given number of days.
     *
     * @param memberId member identifier
     * @param sortDirection     number of days to look back
     * @return wrapper DTO containing list of PR progress entries
     *
     * <p>Returns {@link HttpStatus#ACCEPTED} when records are fetched successfully.</p>
     */
    @LogExecutionTime
    @GetMapping("/getPrs")
    ResponseEntity<MemberPrProgressWrapperDto> getAllPrProgress(
            @RequestParam @NotBlank(message = "Can not proceed with empty Member ID") String memberId,
            @RequestParam @PositiveOrZero(message = "Page No can not be Negative") int pageNo,
            @RequestParam @Positive(message = "PageSize can not be Negative") int pageSize,
            @RequestParam(required = false) String searchBy,
            @RequestParam(defaultValue= "DESC") String sortDirection,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to)
    {
        log.info("Request received to get Prf records for member {} of pageNo {} with size {} searchBy {} with direction {} from date {} to date {}",
                memberId,pageNo,pageSize,searchBy,sortDirection,from,to);
        MemberPrProgressWrapperDto response = prService
                .getAllPrProgress(memberId,pageNo,pageSize,searchBy,sortDirection,from,to);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @LogExecutionTime
    @PutMapping("/updatePr/{workoutName}")
    ResponseEntity<GenericResponse> updatePr(
            @RequestParam @NotBlank(message = "Can not Proceed Without Any Valid MemberId") String  memberId,
            @PathVariable @NotBlank(message = "Please Provide a Workout Name") String workoutName,
            @Valid @RequestBody UpdatePrRequestDto requestDto
    ) {
        log.info("Request received to update workout {} for member {}", workoutName, memberId);
        GenericResponse response = prService.updateExistingPr(memberId,workoutName,requestDto);
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
    @DeleteMapping("/deleteOneDay")
    ResponseEntity<GenericResponse> deletePrByIdDate(
            @RequestParam @NotBlank(message = "Can not Proceed with empty memberId") String memberId,
            @RequestParam @PastOrPresent(message = "Can not delete a pr from future") LocalDate date)
    {
        String response = prService.deleteByIdAndDate(memberId,date);
        return ResponseEntity.status(HttpStatus.OK).body(new GenericResponse(response));
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
    ResponseEntity<GenericResponse> deletePrByWorkoutName(@RequestParam String memberId,
                                               @RequestParam
                                               @PastOrPresent(message = "Can not delete a pr from future")
                                               LocalDate date,
                                               @PathVariable String workoutName)
    {
        String response = prService.deleteByWorkoutNameWIthMemberIdAndDate(memberId,date,workoutName);
        return ResponseEntity.status(HttpStatus.OK).body(new GenericResponse(response));
    }
    /**
     * Retrieve pre-aggregated monthly BMI summary for a given member.
     *
     * @param memberId member identifier
     * @return monthly BMI summary report
     *
     * <p>Returns {@link HttpStatus#OK} along with cached data (via Redis) if available.</p>
     */
    @LogExecutionTime
    @GetMapping("/bmiSummary")
    ResponseEntity<BmiSummaryResponseWrapperDto> getMonthlyBmiReport(
             @RequestParam @NotBlank(message = "please verify your identity to view summaries") String memberId,
             @RequestParam @PositiveOrZero(message = "Can not proceed negative page numbers please enter valid page no again") int pageNo,
             @RequestParam @PositiveOrZero(message = "Can not proceed negative page sizes please enter a valid page size again" ) int pageSize)

    {
        log.info("request received on ⌛⌛ {} to get bmi summaries for member {} for  {}  for {} days",
                LocalDateTime.now().format(formatter), memberId,pageNo, pageSize);
        BmiSummaryResponseWrapperDto response = fitService.getBmiReportByMonth(memberId,pageNo,pageSize);
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
    @LogExecutionTime
    @GetMapping("/prSummary")
    ResponseEntity<?> getMonthlyPrReport(
            @RequestParam @NotBlank(message = "Can not proceed with empty Member ID") String memberId,
            @RequestParam @PositiveOrZero(message = "Page No can not be Negative") int pageNo,
            @RequestParam @Positive(message = "PageSize can not be Negative") int pageSize,
            @RequestParam(required = false) String searchBy,
            @RequestParam(defaultValue= "DESC") String sortDirection,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to){
        log.info("Request received to get pr records for member {} from {} to {}",
                memberId,from,to);
        Object response = prService.getPrReportByMonth(memberId,pageNo,pageSize,searchBy,sortDirection,from,to);
        return  ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
